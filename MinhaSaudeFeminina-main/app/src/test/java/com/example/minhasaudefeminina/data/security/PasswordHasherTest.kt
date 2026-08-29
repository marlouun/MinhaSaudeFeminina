package com.example.minhasaudefeminina.data.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun `senha correta valida e senha incorreta falha`() {
        val digest = hasher.create("Senha123".toCharArray())

        assertTrue(
            hasher.verify(
                "Senha123".toCharArray(),
                digest.hash,
                digest.salt,
                digest.algorithm,
                digest.iterations
            )
        )
        assertFalse(
            hasher.verify(
                "Senha124".toCharArray(),
                digest.hash,
                digest.salt,
                digest.algorithm,
                digest.iterations
            )
        )
    }

    @Test
    fun `mesma senha recebe salts diferentes`() {
        val first = hasher.create("Senha123".toCharArray())
        val second = hasher.create("Senha123".toCharArray())

        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
    }
}
