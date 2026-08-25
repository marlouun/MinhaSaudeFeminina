package com.example.minhasaudefeminina.model

enum class FaseVida(val label: String) {
    ADOLESCENCIA("Adolescencia"),
    IDADE_REPRODUTIVA("Idade reprodutiva"),
    GESTACAO("Gestacao"),
    CLIMATERIO("Climaterio"),
    MENOPAUSA("Menopausa"),
    SENESCENCIA("Senescencia")
}

enum class SintomaTipo(val label: String) {
    MENSTRUACAO("Menstruacao"),
    COLICA("Colica"),
    CORRIMENTO("Corrimento"),
    SANGRAMENTO("Sangramento"),
    SINTOMA_URINARIO("Sintoma urinario"),
    HUMOR_TPM("Humor/TPM"),
    FOGACHOS("Fogachos"),
    SUOR_NOTURNO("Suor noturno"),
    OUTRO("Outro")
}

enum class ArtigoStatus {
    RASCUNHO,
    PUBLICADO
}

data class Usuario(
    val id: String,
    val nome: String,
    val email: String,
    val criadoEm: Long,
    val atualizadoEm: Long
)

data class PerfilUsuario(
    val usuarioId: String,
    val faseVida: FaseVida = FaseVida.IDADE_REPRODUTIVA,
    val estaGestante: Boolean = false,
    val dataPapanicolau: Long? = null,
    val dataMamografia: Long? = null,
    val fotoUri: String? = null
)

data class RegistroSintoma(
    val id: String,
    val usuarioId: String,
    val dataTimestamp: Long,
    val tipo: SintomaTipo,
    val intensidade: Int,
    val notas: String?,
    val criadoEm: Long,
    val atualizadoEm: Long
)

data class MensagemChat(
    val id: String,
    val usuarioId: String,
    val sessaoId: String,
    val texto: String,
    val isUsuario: Boolean,
    val enviadoEm: Long
)

data class Artigo(
    val id: String,
    val slug: String,
    val categoria: String,
    val titulo: String,
    val subtitulo: String,
    val resumo: String,
    val conteudoJson: String,
    val autor: String,
    val tags: List<String>,
    val capaUri: String?,
    val status: ArtigoStatus,
    val criadoEm: Long,
    val atualizadoEm: Long,
    val publicadoEm: Long?
)
