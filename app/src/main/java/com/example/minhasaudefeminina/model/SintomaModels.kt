package com.example.minhasaudefeminina.model

import com.google.firebase.database.IgnoreExtraProperties
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

@IgnoreExtraProperties
data class Usuario(
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val email: String = "",
    val foto_url: String? = null,
    val criado_em: Long = System.currentTimeMillis()
)

@IgnoreExtraProperties
data class PerfilUsuario(
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val fase_vida: String = FaseVida.IDADE_REPRODUTIVA.name,
    val data_papanicolau: Long? = null,
    val data_mamografia: Long? = null
)

@IgnoreExtraProperties
data class RegistroSintoma(
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val data_timestamp: Long = System.currentTimeMillis(),
    val tipo: String = "",
    val intensidade: Int = 1,
    val notas: String? = null
)

@IgnoreExtraProperties
data class DadosGestacao(
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val esta_gestante: Boolean = false,
    val data_dum_timestamp: Long? = null
)

@IgnoreExtraProperties
data class MensagemChat(
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String? = null,
    val sessao_id: String = UUID.randomUUID().toString(),
    val texto: String = "",
    val is_usuario: Boolean = true,
    val enviado_em: Long = System.currentTimeMillis()
)

data class Artigo(
    val id: String = "",
    val categoria: String = "",
    val titulo: String = "",
    val resumo: String = "",
    val conteudo: String = "",
    val referencias: List<String> = emptyList()
)
