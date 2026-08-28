package com.example.minhasaudefeminina.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Protecao local temporaria. O hash dificulta a leitura direta da senha, mas
 * autenticacao somente no dispositivo nao substitui um servidor seguro.
 */
class PasswordHasher(
    private val secureRandom: SecureRandom = SecureRandom()
) {
    data class Digest(
        val hash: String,
        val salt: String,
        val algorithm: String,
        val iterations: Int
    )

    fun create(password: CharArray): Digest {
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val hash = derive(password, salt, ALGORITHM, ITERATIONS)
        return Digest(
            hash = Base64.getEncoder().withoutPadding().encodeToString(hash),
            salt = Base64.getEncoder().withoutPadding().encodeToString(salt),
            algorithm = ALGORITHM,
            iterations = ITERATIONS
        )
    }

    fun verify(
        password: CharArray,
        expectedHash: String,
        encodedSalt: String,
        algorithm: String,
        iterations: Int
    ): Boolean {
        return runCatching {
            val salt = Base64.getDecoder().decode(encodedSalt)
            val expected = Base64.getDecoder().decode(expectedHash)
            val actual = derive(password, salt, algorithm, iterations)
            MessageDigest.isEqual(expected, actual)
        }.getOrDefault(false)
    }

    private fun derive(
        password: CharArray,
        salt: ByteArray,
        algorithm: String,
        iterations: Int
    ): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA1"
        const val ITERATIONS = 120_000
        private const val SALT_BYTES = 16
        private const val KEY_LENGTH_BITS = 256
    }
}
