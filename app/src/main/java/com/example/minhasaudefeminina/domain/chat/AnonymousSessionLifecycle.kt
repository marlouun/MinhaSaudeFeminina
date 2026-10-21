package com.example.minhasaudefeminina.domain.chat

internal data class AnonymousTokens(val access: String, val refresh: String, val expiresAt: Long)
internal class SessionRefreshRejected : Exception()

/** Serializes sign-up/refresh and preserves identity through transient failures. */
internal class AnonymousSessionLifecycle(
    private val restore: () -> AnonymousTokens?,
    private val persist: (AnonymousTokens) -> Unit,
    private val refresh: (String) -> AnonymousTokens,
    private val create: () -> AnonymousTokens,
    private val remove: () -> Unit,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private var cached: AnonymousTokens? = null

    @Synchronized fun accessToken(): String {
        val current = cached ?: restore()
        if (current != null && current.expiresAt > now() + 60_000) {
            cached = current
            return current.access
        }
        if (current != null) {
            try { return save(refresh(current.refresh)) }
            catch (_: SessionRefreshRejected) {
                remove()
                cached = null
            }
        }
        return save(create())
    }

    private fun save(tokens: AnonymousTokens): String {
        persist(tokens) // Never use a session that failed to persist securely.
        cached = tokens
        return tokens.access
    }

    @Synchronized fun invalidateAccessToken() {
        cached = (cached ?: restore())?.copy(expiresAt = 0)
    }

    @Synchronized fun clearVolatile() { cached = null }
}
