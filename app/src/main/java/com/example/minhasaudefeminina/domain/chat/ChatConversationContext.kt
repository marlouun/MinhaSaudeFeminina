package com.example.minhasaudefeminina.domain.chat

import com.example.minhasaudefeminina.model.MensagemChat

data class ChatContextMessage(val role: String, val text: String)

/** Only this local account's recent chat, bounded by the existing backend contract. */
fun buildChatContext(messages: List<MensagemChat>, userId: String): List<ChatContextMessage> =
    messages.asSequence()
        .filter { it.usuarioId == userId && it.texto.isNotBlank() }
        .map { ChatContextMessage(if (it.isUsuario) "user" else "model", it.texto.trim().take(500)) }
        .toList()
        .takeLast(6)
        .dropWhile { it.role != "user" }
