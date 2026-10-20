package com.example.minhasaudefeminina.domain.chat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
class PreparedChatResponsesTest {
 @Test fun knownQuestionsRemainOffline() {
  assertNotNull(PreparedChatResponses.findAnswer("O que fazer com cólica?"))
  assertNotNull(PreparedChatResponses.findAnswer("Sinto ardor ao urinar"))
 }
 @Test fun newQuestionsUseAi() {
  assertNull(PreparedChatResponses.findAnswer("Como posso melhorar meu sono?"))
 }
 @Test fun urgentQuestionsHavePriority() {
  assertTrue(PreparedChatResponses.findAnswer("Estou grávida e com sangramento intenso")!!.contains("192"))
  assertTrue(PreparedChatResponses.findAnswer("Quero me matar")!!.contains("188"))
 }
}