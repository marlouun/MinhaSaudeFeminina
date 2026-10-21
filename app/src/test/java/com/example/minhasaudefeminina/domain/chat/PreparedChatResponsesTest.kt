package com.example.minhasaudefeminina.domain.chat

import org.junit.Assert.*
import org.junit.Test

class PreparedChatResponsesTest {
    @Test fun typedTextNeverSelectsPreparedAnswersByKeywordOrExactQuestion() {
        val texts = listOf("O que é o câncer de mama?", "Minha menstruação não está atrasada", "Quais diferenças entre pílula e DIU?") +
            PreparedChatResponses.suggestions.map { it.question }
        texts.forEach { text ->
            val route = PreparedChatResponses.route(ChatInput.Typed(text))!!
            assertEquals(text, route.question)
            assertNull(route.preparedAnswer)
        }
    }
    @Test fun onlyExplicitSuggestionsReturnTheirOwnOfflineAnswers() {
        PreparedChatResponses.suggestions.forEach { item ->
            val route = PreparedChatResponses.route(ChatInput.Suggested(item.id))!!
            assertEquals(item.question, route.question)
            assertEquals(item.answer, route.preparedAnswer)
        }
        assertEquals(6, PreparedChatResponses.suggestions.map { it.id }.toSet().size)
    }
    @Test fun invalidSuggestionAndBlankInputDoNotSendAnything() {
        assertNull(PreparedChatResponses.route(ChatInput.Suggested("mama")))
        assertNull(PreparedChatResponses.route(ChatInput.Typed("  ")))
        assertEquals(500, PreparedChatResponses.route(ChatInput.Typed("x".repeat(700)))!!.question.length)
    }
}
