package com.example.minhasaudefeminina.data.remote

import com.example.minhasaudefeminina.domain.chat.AnonymousSessionLifecycle
import com.example.minhasaudefeminina.domain.chat.sessionFailureMessage
import com.example.minhasaudefeminina.domain.chat.AnonymousTokens
import com.example.minhasaudefeminina.domain.chat.SessionRefreshRejected
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * No separate email/password. Supabase anonymous JWTs are created on demand.
 * The refresh token is persisted encrypted in the Android Keystore so the same
 * anonymous identity (and server-side daily quota) survives app restarts.
 */
class ChatAiSession(context: Context, localUserId: String) {
    private val preferences = context.applicationContext.getSharedPreferences("chat_ai_identity_v1", Context.MODE_PRIVATE)
    private val preferenceKey = "account_" + localUserId
    private val keyAlias = "msf_chat_ai_aes_v1"

    private val lifecycle = AnonymousSessionLifecycle(
        restore = ::restore,
        persist = ::save,
        refresh = { refreshToken ->
            try {
                parseTokens(request("/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", refreshToken)))
            } catch (failure: AuthHttpException) {
                if (failure.status in listOf(400, 401)) throw SessionRefreshRejected()
                throw failure
            }
        },
        create = {
            val payload = request("/auth/v1/signup", JSONObject().put("data", JSONObject()))
            check(payload.optJSONObject("user")?.optBoolean("is_anonymous") == true) {
                "A autenticação anônima não foi iniciada."
            }
            parseTokens(payload)
        },
        remove = { preferences.edit().remove(preferenceKey).commit(); Unit },
    )

    suspend fun accessToken(): String = withContext(Dispatchers.IO) {
        try { lifecycle.accessToken() }
        catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (failure: ChatAiException) { throw failure }
        catch (failure: AuthHttpException) {
            throw ChatAiException(failure.status,
                "Não foi possível renovar a sessão da IA (IA-AUTH-${failure.status}). Tente novamente; as respostas prontas continuam disponíveis.")
        }
        catch (failure: Exception) { throw ChatAiException(0, sessionFailureMessage(failure)) }
    }
    fun invalidateAccessToken() = lifecycle.invalidateAccessToken()
    fun clearVolatile() = lifecycle.clearVolatile()

    private fun save(tokens: AnonymousTokens) {
        val raw = JSONObject()
            .put("access", tokens.access).put("refresh", tokens.refresh)
            .put("expiresAt", tokens.expiresAt).toString()
        check(preferences.edit().putString(preferenceKey, encrypt(raw)).commit()) {
            "Não foi possível salvar a sessão segura."
        }
    }

    private fun restore(): AnonymousTokens? {
        val encrypted = preferences.getString(preferenceKey, null) ?: return null
        return try {
            val json = JSONObject(decrypt(encrypted))
            AnonymousTokens(
                json.getString("access"), json.getString("refresh"), json.getLong("expiresAt")
            )
        } catch (_: Exception) {
            // Keystore invalidation or corrupt data; do not use corrupt tokens.
            preferences.edit().remove(preferenceKey).commit()
            null
        }
    }

    private fun parseTokens(payload: JSONObject): AnonymousTokens {
        val access = payload.optString("access_token")
        val refresh = payload.optString("refresh_token")
        if (access.isBlank() || refresh.isBlank()) throw IllegalStateException("Sessão anônima inválida.")
        val ttl = payload.optLong("expires_in", 3600L).coerceAtLeast(120L)
        return AnonymousTokens(access, refresh, System.currentTimeMillis() + ttl * 1000)
    }

    private fun request(path: String, body: JSONObject): JSONObject {
        val conn = (URL(SupabaseConfig.PROJECT_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            if (conn.responseCode !in 200..299) {
                if (path == "/auth/v1/signup" && conn.responseCode == 429) {
                    throw ChatAiException(429, "Limite de criação de sessões atingido (IA-CADASTRO-429). Aguarde e tente novamente; as respostas prontas continuam disponíveis.")
                }
                if (path == "/auth/v1/signup" && conn.responseCode in 400..499) {
                    throw ChatAiException(conn.responseCode,
                        "Não foi possível iniciar a sessão da IA (IA-CADASTRO-${conn.responseCode}). As respostas prontas continuam disponíveis."
                    )
                }
                throw AuthHttpException(conn.responseCode)
            }
            return JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
        } finally {
            conn.disconnect()
        }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(
            keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val parts = value.split(":", limit = 2)
        require(parts.size == 2)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val bytes = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(bytes), Charsets.UTF_8)
    }

    private class AuthHttpException(val status: Int) : Exception("Falha de autenticação anônima: " + status)
}
