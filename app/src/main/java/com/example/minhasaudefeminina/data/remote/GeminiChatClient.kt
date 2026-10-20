package com.example.minhasaudefeminina.data.remote

import com.example.minhasaudefeminina.model.MensagemChat
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Nunca chame Gemini diretamente do APK: a chave fica no Supabase. */
class GeminiChatClient {
    suspend fun respond(question: String, history: List<MensagemChat>, accessToken: String): String = withContext(Dispatchers.IO) {
        val url = URL(SupabaseConfig.PROJECT_URL + "/functions/v1/chat-gemini")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer " + accessToken)
        }
        try {
            // Keep the conversation on the device: do not resend older health messages.
            val request = JSONObject()
                .put("message", question.take(500))
                .put("consent", true)
            connection.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode !in 200..299) {
                val status = connection.responseCode
                throw ChatAiException(status, when (status) {
                    401 -> "Sua sessão anônima da IA expirou. Tente novamente."
                    403 -> "Não foi possível validar a sessão anônima da IA."
                    429 -> "O limite de consultas da IA foi atingido. Tente novamente mais tarde."
                    else -> "A IA está indisponível agora. Tente mais tarde; as respostas prontas continuam disponíveis."
                })
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            JSONObject(body).optString("answer").takeIf(String::isNotBlank)
                ?: throw IllegalStateException("Resposta vazia")
        } finally {
            connection.disconnect()
        }
    }
}

class ChatAiException(val status: Int, message: String) : Exception(message)
