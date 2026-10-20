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
import com.example.minhasaudefeminina.data.repository.ChatRepository
import com.example.minhasaudefeminina.domain.chat.PreparedChatResponses
import com.example.minhasaudefeminina.model.MensagemChat
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val userId: String,
    context: Context,
    private val ai: GeminiChatClient = GeminiChatClient()
) : ViewModel() {
    private val sessionId = UUID.randomUUID().toString()
    private val preferences = context.applicationContext.getSharedPreferences("ai_consent_v2", Context.MODE_PRIVATE)
    private val consentKey = "consent_" + userId
    private val _consentGranted = MutableStateFlow(preferences.getBoolean(consentKey, false))
    val consentGranted: StateFlow<Boolean> = _consentGranted.asStateFlow()
    private val _pendingAiQuestion = MutableStateFlow<String?>(null)
    val pendingAiQuestion: StateFlow<String?> = _pendingAiQuestion.asStateFlow()
    private val aiSession = ChatAiSession(context, userId)

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
        processMessage(clean, ready, useAi = ready == null)
    }

    fun acceptAiConsent() {
        val pending = _pendingAiQuestion.value ?: return
        preferences.edit().putBoolean(consentKey, true).apply()
        _consentGranted.value = true
        _pendingAiQuestion.value = null
        processMessage(pending, null, useAi = true)
    }

    fun revokeAiConsent() {
        if (_isTyping.value) return
        preferences.edit().putBoolean(consentKey, false).apply()
        _consentGranted.value = false
        _pendingAiQuestion.value = null
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
                repository.saveMessage(newMessage(question, true))
                val answer = if (!useAi) prepared ?: PreparedChatResponses.OFFLINE_FALLBACK
                    else try {
                        ai.respond(question, emptyList(), aiSession.accessToken())
                    } catch (c: CancellationException) {
                        throw c
                    } catch (e: ChatAiException) {
                        if (e.status == 401) aiSession.invalidateAccessToken()
                        e.message ?: PreparedChatResponses.OFFLINE_FALLBACK
                    } catch (e: IllegalStateException) {
                        if (e.message?.contains("acesso anônimo") == true) e.message!!
                        else PreparedChatResponses.OFFLINE_FALLBACK
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
        aiSession.clearVolatile()
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
