package com.example.minhasaudefeminina.model

import java.util.Date
import java.util.UUID

enum class FaseVida(val label: String) {
    ADOLESCENCIA("Adolescência"),
    IDADE_REPRODUTIVA("Idade Reprodutiva"),
    GESTACAO("Gestação"),
    CLIMATERIO("Climatério"),
    MENOPAUSA("Menopausa"),
    SENESCENCIA("Senescência")
}

enum class SintomaTipo(val label: String) {
    MENSTRUACAO("Menstruação"),
    COLICA("Cólica"),
    CORRIMENTO("Corrimento"),
    SANGRAMENTO("Sangramento"),
    SINTOMA_URINARIO("Sintoma Urinário"),
    HUMOR_TPM("Humor/TPM"),
    FOGACHOS("Fogachos"),
    SUOR_NOTURNO("Suor Noturno"),
    OUTRO("Outro")
}

data class RegistroSintoma(
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String,
    val data: Date = Date(),
    val tipo: SintomaTipo,
    val intensidade: Int, // 1 a 5
    val notas: String = ""
)
