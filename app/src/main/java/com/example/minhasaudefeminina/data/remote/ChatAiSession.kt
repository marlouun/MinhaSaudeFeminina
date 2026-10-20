package com.example.minhasaudefeminina.data.remote

import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/** Chat-only session, kept in memory. Passwords and refresh tokens are never saved. */
class ChatAiSession {
    @Volatile private var accessToken: String? = null
    @Volatile private var expiresAt: Long = 0

    fun token(): String? = accessToken?.takeIf { System.currentTimeMillis() < expiresAt }

    fun clear() {
        accessToken = null
        expiresAt = 0
    }

    // Called on Dispatchers.IO by the ViewModel.
    fun signIn(email: String, password: String) {
        val connection = (URL(SupabaseConfig.PROJECT_URL + "/auth/v1/token?grant_type=password")
            .openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            val payload = JSONObject().put("email", email.trim()).put("password", password)
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            if (connection.responseCode !in 200..299) throw IllegalStateException("Não foi possível entrar. Confira a conta de acesso à IA e sua conexão.")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val session = JSONObject(body)
            val user = session.optJSONObject("user")
            if (user == null || user.optBoolean("is_anonymous") || user.optString("email_confirmed_at").isBlank()) {
                throw IllegalStateException("Confirme o e-mail da conta de acesso à IA.")
            }
            val token = session.optString("access_token").takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Sessão inválida.")
            expiresAt = System.currentTimeMillis() + (session.optLong("expires_in", 3600) - 60).coerceAtLeast(0) * 1000
            accessToken = token
        } finally {
            connection.disconnect()
        }
    }
}
