package com.example.minhasaudefeminina.data.remote

import com.example.minhasaudefeminina.model.MensagemChat
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
            val safeHistory = JSONArray()
            history.takeLast(6).forEach { item ->
                safeHistory.put(JSONObject()
                    .put("role", if (item.isUsuario) "user" else "model")
                    .put("text", item.texto.take(500)))
            }
            val request = JSONObject()
                .put("message", question.take(500))
                .put("consent", true)
                .put("history", safeHistory)
            connection.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode !in 200..299) {
                val status = connection.responseCode
                throw ChatAiException(status, when (status) {
                    401 -> "Sua sessão da IA expirou. Entre novamente."
                    403 -> "Esta conta ainda não tem acesso à IA. Solicite a liberação à equipe do aplicativo."
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
