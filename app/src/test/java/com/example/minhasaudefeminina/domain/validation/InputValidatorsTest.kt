package com.example.minhasaudefeminina.domain.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class InputValidatorsTest {
    @Test
    fun `email valido passa e email incompleto falha`() {
        assertNull(InputValidators.emailError("pessoa@example.com"))
        assertNotNull(InputValidators.emailError("pessoa@"))
    }

    @Test
    fun `senha exige tamanho letra e numero`() {
        assertNull(InputValidators.passwordError("Senha123"))
        assertNotNull(InputValidators.passwordError("12345678"))
        assertNotNull(InputValidators.passwordError("senhasemnumero"))
        assertNotNull(InputValidators.passwordError("A1b"))
    }

    @Test
    fun `notas respeitam limite`() {
        assertNull(InputValidators.notesError("texto curto"))
        assertNotNull(InputValidators.notesError("a".repeat(1_001)))
    }
}
