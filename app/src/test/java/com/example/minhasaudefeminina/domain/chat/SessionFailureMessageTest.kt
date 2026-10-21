package com.example.minhasaudefeminina.domain.chat

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import javax.net.ssl.SSLException
import org.junit.Assert.*
import org.junit.Test

class SessionFailureMessageTest {
    @Test fun failuresHaveDistinctSafeActionableCodes() {
        val cases = listOf(
            UnknownHostException("private") to "IA-DNS",
            SocketTimeoutException("private") to "IA-TEMPO",
            SSLException("private") to "IA-TLS",
            GeneralSecurityException("private") to "IA-SEGURA",
            IOException("private") to "IA-REDE",
            IllegalStateException("private") to "IA-SESSAO",
        )
        cases.forEach { (failure, code) ->
            val text = sessionFailureMessage(failure)
            assertTrue(text.contains(code))
            assertFalse(text.contains("private"))
        }
    }
}
