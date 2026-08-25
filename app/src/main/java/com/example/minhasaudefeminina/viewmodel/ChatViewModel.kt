package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.repository.ChatRepository
import com.example.minhasaudefeminina.model.MensagemChat
import java.text.Normalizer
import java.util.UUID
import java.util.regex.Pattern
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val userId: String
) : ViewModel() {
    private val sessionId = UUID.randomUUID().toString()

    val messages: StateFlow<List<MensagemChat>> = repository.observeMessages(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        viewModelScope.launch { repository.ensureGreeting(userId, sessionId) }
    }

    fun sendMessage(text: String) {
        val cleanText = text.trim()
        if (cleanText.isEmpty() || _isTyping.value) return
        viewModelScope.launch {
            repository.saveMessage(newMessage(cleanText, true))
            _isTyping.value = true
            delay(650)
            repository.saveMessage(newMessage(generateResponse(cleanText), false))
            _isTyping.value = false
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory(userId)
            repository.ensureGreeting(userId, sessionId)
        }
    }

    private fun newMessage(text: String, isUser: Boolean) = MensagemChat(
        id = UUID.randomUUID().toString(),
        usuarioId = userId,
        sessaoId = sessionId,
        texto = text,
        isUsuario = isUser,
        enviadoEm = System.currentTimeMillis()
    )

    private fun normalize(text: String): String {
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
            .matcher(nfd)
            .replaceAll("")
            .lowercase()
    }

    private fun generateResponse(query: String): String {
        val q = normalize(query)
        return when {
            q.matches(Regex("(oi|ola|bom dia|boa tarde|boa noite).*")) ->
                "Olá! Posso explicar informações gerais e ajudar você a organizar sintomas. Em caso de urgência, procure atendimento imediatamente."
            q.contains("corrimento") ->
                "Mudanças de cor, cheiro, quantidade, coceira ou dor merecem avaliação na UBS. Evite automedicação e registre quando os sintomas começaram."
            q.contains("colica") || (q.contains("dor") && q.contains("menstr")) ->
                "Calor local e movimento leve podem ajudar em cólicas leves. Dor incapacitante, febre, desmaio ou piora rápida são sinais para procurar atendimento."
            q.contains("atraso") || q.contains("gravid") ->
                "Atrasos podem ter várias causas. Se houver possibilidade de gravidez, faça um teste conforme a orientação do produto e procure a UBS se o resultado for positivo ou a dúvida persistir."
            q.contains("urina") || q.contains("xixi") || q.contains("ardor") ->
                "Ardor, urgência ou dor para urinar podem indicar um problema urinário. Febre, sangue na urina ou dor nas costas exigem avaliação rápida."
            q.contains("violencia") || q.contains("agress") || q.contains("abuso") ->
                "Você não está sozinha. Ligue 180 para orientação. Se houver risco imediato, ligue 190 ou procure um local seguro."
            q.contains("anticoncepc") || q.contains("diu") || q.contains("pilula") ->
                "A escolha do método depende do histórico e das necessidades de cada pessoa. A UBS oferece orientação de planejamento reprodutivo; não inicie ou interrompa hormônios apenas com base no chat."
            q.contains("mama") || q.contains("caroco") || q.contains("nodulo") ->
                "Uma alteração nova na mama, secreção espontânea, retração da pele ou nódulo persistente deve ser avaliado por um profissional."
            else ->
                "Essa dúvida é importante. Anote quando começou, frequência, intensidade e sinais associados. Leve o registro à UBS, pois o chat não consegue diagnosticar nem prescrever tratamento."
        }
    }

    companion object {
        fun factory(repository: ChatRepository, userId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ChatViewModel(repository, userId) }
        }
    }
}
