package com.example.minhasaudefeminina.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.remote.GeminiChatClient
import com.example.minhasaudefeminina.data.remote.ChatAiSession
import com.example.minhasaudefeminina.data.remote.ChatAiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.minhasaudefeminina.data.repository.ChatRepository
import com.example.minhasaudefeminina.domain.chat.PreparedChatResponses
import com.example.minhasaudefeminina.model.MensagemChat
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val userId: String,
    context: Context,
    private val ai: GeminiChatClient = GeminiChatClient()
) : ViewModel() {
    private val sessionId = UUID.randomUUID().toString()
    private val preferences = context.applicationContext.getSharedPreferences("ai_consent_v1", Context.MODE_PRIVATE)
    private val consentKey = "consent_" + userId
    private val _consentGranted = MutableStateFlow(preferences.getBoolean(consentKey, false))
    val consentGranted: StateFlow<Boolean> = _consentGranted.asStateFlow()
    private val _pendingAiQuestion = MutableStateFlow<String?>(null)
    val pendingAiQuestion: StateFlow<String?> = _pendingAiQuestion.asStateFlow()
    private val aiSession = ChatAiSession()
    private val _aiLoginRequired = MutableStateFlow(false)
    val aiLoginRequired = _aiLoginRequired.asStateFlow()
    private val _aiLoginLoading = MutableStateFlow(false)
    val aiLoginLoading = _aiLoginLoading.asStateFlow()
    private val _aiLoginError = MutableStateFlow<String?>(null)
    val aiLoginError = _aiLoginError.asStateFlow()

    fun signInToAi(email: String, password: String) {
        if (_aiLoginLoading.value) return
        viewModelScope.launch {
            _aiLoginLoading.value = true
            _aiLoginError.value = null
            try {
                withContext(Dispatchers.IO) { aiSession.signIn(email, password) }
                _aiLoginRequired.value = false
                val pending = _pendingAiQuestion.value
                _pendingAiQuestion.value = null
                if (pending != null) processMessage(pending, null, useAi = true)
            } catch (c: CancellationException) { throw c }
            catch (_: Exception) {
                _aiLoginError.value = "Não foi possível entrar. Use uma conta confirmada de acesso à IA e confira sua conexão."
            } finally { _aiLoginLoading.value = false }
        }
    }

    fun clearAiSession() { aiSession.clear() }

    fun cancelAiLogin() {
        if (_aiLoginLoading.value) return
        _aiLoginRequired.value = false
        declineAiConsent()
    }

    val messages: StateFlow<List<MensagemChat>> = repository.observeMessages(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        viewModelScope.launch { repository.ensureGreeting(userId, sessionId) }
    }

    fun sendMessage(text: String) {
        val clean = text.trim().take(500)
        if (clean.isBlank() || _isTyping.value || _pendingAiQuestion.value != null) return
        val ready = PreparedChatResponses.findAnswer(clean)
        if (ready == null && !_consentGranted.value) {
            _pendingAiQuestion.value = clean
            return
        }
        if (ready == null && aiSession.token() == null) {
            _pendingAiQuestion.value = clean
            _aiLoginRequired.value = true
            return
        }
        processMessage(clean, ready, useAi = ready == null)
    }

    fun acceptAiConsent() {
        val pending = _pendingAiQuestion.value ?: return
        preferences.edit().putBoolean(consentKey, true).apply()
        _consentGranted.value = true
        if (aiSession.token() == null) {
            _aiLoginRequired.value = true
            return
        }
        _pendingAiQuestion.value = null
        processMessage(pending, null, useAi = true)
    }

    fun declineAiConsent() {
        val pending = _pendingAiQuestion.value ?: return
        _pendingAiQuestion.value = null
        processMessage(pending, PreparedChatResponses.DECLINED_FALLBACK, useAi = false)
    }

    private fun processMessage(question: String, prepared: String?, useAi: Boolean) {
        viewModelScope.launch {
            _isTyping.value = true
            try {
                // Historical context is built before persisting the new message, avoiding duplication.
                val history = if (useAi) messages.first().takeLast(6) else emptyList()
                repository.saveMessage(newMessage(question, true))
                val answer = if (!useAi) prepared ?: PreparedChatResponses.OFFLINE_FALLBACK
                    else try {
                        ai.respond(question, history, aiSession.token() ?: throw ChatAiException(401, "Entre na conta de acesso à IA."))
                    } catch (c: CancellationException) {
                        throw c
                    } catch (e: ChatAiException) {
                        if (e.status == 401) aiSession.clear()
                        e.message ?: PreparedChatResponses.OFFLINE_FALLBACK
                    } catch (_: Exception) {
                        PreparedChatResponses.OFFLINE_FALLBACK
                    }
                repository.saveMessage(newMessage(answer, false))
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun clearHistory() {
        if (_isTyping.value) return
        _pendingAiQuestion.value = null
        viewModelScope.launch {
            repository.clearHistory(userId)
            repository.ensureGreeting(userId, sessionId)
        }
    }

    override fun onCleared() {
        aiSession.clear()
        super.onCleared()
    }

    private fun newMessage(text: String, isUser: Boolean) = MensagemChat(
        id = UUID.randomUUID().toString(),
        usuarioId = userId,
        sessaoId = sessionId,
        texto = text,
        isUsuario = isUser,
        enviadoEm = System.currentTimeMillis()
    )

    companion object {
        fun factory(repository: ChatRepository, userId: String, context: Context): ViewModelProvider.Factory =
            viewModelFactory { initializer { ChatViewModel(repository, userId, context) } }
    }
}
