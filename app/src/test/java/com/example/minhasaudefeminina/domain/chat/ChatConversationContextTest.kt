package com.example.minhasaudefeminina.domain.chat

import com.example.minhasaudefeminina.model.MensagemChat
import org.junit.Assert.*
import org.junit.Test

class ChatConversationContextTest {
    private fun message(text: String, user: Boolean, owner: String = "local-user") =
        MensagemChat(text, owner, "session", text, user, 0)

    @Test fun preparedAnswerAndItsQuestionReachTheAiAsContext() {
        val question = "O que fazer com cólica?"
        val prepared = PreparedChatResponses.findAnswer(question)!!
        val context = buildChatContext(listOf(
            message("Olá!", false), message(question, true), message(prepared, false)
        ), "local-user")
        assertEquals(listOf("user", "model"), context.map { it.role })
        assertEquals(question, context[0].text)
        assertEquals(prepared.take(500), context[1].text)
        assertFalse(context.any { it.text == "Explique melhor" })
    }

    @Test fun onlyTheCurrentAccountsRecentConversationIsIncluded() {
        val messages = (1..10).map { message("Mensagem $it", it % 2 == 1) } +
            message("Outro usuário", true, "other-user")
        val context = buildChatContext(messages, "local-user")
        assertEquals(6, context.size)
        assertEquals("Mensagem 5", context.first().text)
        assertEquals("Mensagem 10", context.last().text)
        assertFalse(context.any { it.text.contains("Outro") })
    }

    @Test fun backendBoundsAndUserFirstRoleArePreserved() {
        val context = buildChatContext(listOf(message("greeting", false),
            message(" ", true), message("x".repeat(700), true), message("reply", false)), "local-user")
        assertEquals(2, context.size)
        assertEquals("user", context.first().role)
        assertEquals(500, context.first().text.length)
        assertTrue(buildChatContext(emptyList(), "local-user").isEmpty())
    }

    @Test fun clearingLocalHistoryLeavesNoPriorContext() {
        assertTrue(buildChatContext(listOf(message("Olá!", false)), "local-user").isEmpty())
    }
}
