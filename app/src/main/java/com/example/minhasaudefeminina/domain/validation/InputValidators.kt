package com.example.minhasaudefeminina.domain.validation

object InputValidators {
    private val emailRegex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    fun nameError(value: String): String? = when {
        value.trim().length < 2 -> "Informe um nome com pelo menos 2 caracteres."
        value.trim().length > 80 -> "O nome pode ter no maximo 80 caracteres."
        else -> null
    }

    fun emailError(value: String): String? = when {
        value.isBlank() -> "Informe o e-mail."
        value.length > 254 -> "O e-mail e muito longo."
        !emailRegex.matches(value.trim()) -> "Informe um e-mail valido."
        else -> null
    }

    fun passwordError(value: String): String? = when {
        value.length < 8 -> "A senha deve ter pelo menos 8 caracteres."
        value.length > 128 -> "A senha pode ter no maximo 128 caracteres."
        value.none(Char::isLetter) || value.none(Char::isDigit) -> "Use pelo menos uma letra e um numero."
        else -> null
    }

    fun notesError(value: String): String? =
        if (value.length > 1_000) "As notas podem ter no maximo 1000 caracteres." else null
}
