package com.example.minhasaudefeminina.domain.chat

import java.util.concurrent.Executors
import org.junit.Assert.*
import org.junit.Test

class AnonymousSessionLifecycleTest {
    private val first = AnonymousTokens("access", "refresh", 120_000)
    private val renewed = AnonymousTokens("renewed", "rotated-refresh", 240_000)

    @Test fun restartRestoresSameIdentityWithoutSigningUp() {
        var stored: AnonymousTokens? = null
        var created = 0
        fun client() = AnonymousSessionLifecycle({ stored }, { stored = it },
            { error("must not refresh") }, { created++; first }, { stored = null }, { 0 })
        assertEquals("access", client().accessToken())
        assertEquals("access", client().accessToken())
        assertEquals(1, created)
    }

    @Test fun expiryRotatesTokensInsteadOfCreatingNewIdentity() {
        var stored = first
        val client = AnonymousSessionLifecycle({ stored }, { stored = it },
            { assertEquals("refresh", it); renewed }, { error("must not sign up") }, {}, { 70_000 })
        assertEquals("renewed", client.accessToken())
        assertEquals(renewed, stored)
    }

    @Test fun server401ForcesRefreshEvenBeforeLocalExpiry() {
        val client = AnonymousSessionLifecycle({ first }, {}, { renewed },
            { error("must not sign up") }, {}, { 0 })
        assertEquals("access", client.accessToken())
        client.invalidateAccessToken()
        assertEquals("renewed", client.accessToken())
    }

    @Test fun networkFailureDoesNotCreateAnIdentityToEvadeQuota() {
        var created = 0
        val client = AnonymousSessionLifecycle({ first }, {}, { throw java.io.IOException("offline") },
            { created++; renewed }, {}, { 70_000 })
        assertThrows(java.io.IOException::class.java) { client.accessToken() }
        assertEquals(0, created)
    }

    @Test fun revokedRefreshRemovesRejectedSessionBeforeNewSignUp() {
        var removed = false
        val client = AnonymousSessionLifecycle({ first }, {}, { throw SessionRefreshRejected() },
            { assertTrue(removed); renewed }, { removed = true }, { 70_000 })
        assertEquals("renewed", client.accessToken())
    }

    @Test fun concurrentCallsCreateOneSession() {
        var created = 0
        val client = AnonymousSessionLifecycle({ null }, {}, { error("unexpected") },
            { created++; first }, {}, { 0 })
        val pool = Executors.newFixedThreadPool(8)
        try {
            val results = pool.invokeAll((1..20).map { java.util.concurrent.Callable { client.accessToken() } })
            assertTrue(results.all { it.get() == "access" })
            assertEquals(1, created)
        } finally { pool.shutdownNow() }
    }
}
