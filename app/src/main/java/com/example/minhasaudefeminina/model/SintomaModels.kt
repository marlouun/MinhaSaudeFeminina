package com.example.minhasaudefeminina.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
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

/**
 * Tabela: usuario (DER)
 */
data class Usuario(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val nome: String = "",
    val email: String = "",
    val foto_url: String? = null,
    val criado_em: Timestamp = Timestamp.now(),
    val atualizado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: perfil_usuario (DER)
 */
data class PerfilUsuario(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val fase_vida: String = FaseVida.IDADE_REPRODUTIVA.name,
    val data_papanicolau: Timestamp? = null,
    val data_mamografia: Timestamp? = null,
    val atualizado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: configuracao_usuario (DER)
 */
data class ConfiguracaoUsuario(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val notificacoes: Boolean = true,
    val compartilhar_dados: Boolean = false,
    val atualizado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: registro_sintoma (DER)
 */
data class RegistroSintoma(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val data: Timestamp = Timestamp.now(),
    val tipo: String = "",
    val intensidade: Int = 1,
    val notas: String? = null,
    val criado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: ciclo_menstrual (DER)
 */
data class CicloMenstrual(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val data_inicio: Timestamp = Timestamp.now(),
    val duracao_ciclo_dias: Int = 28,
    val duracao_periodo_dias: Int = 5,
    val notas: String? = null,
    val criado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: dados_gestacao (DER)
 */
data class DadosGestacao(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String = "",
    val esta_gestante: Boolean = false,
    val data_dum: Timestamp? = null,
    val data_parto_prevista: Timestamp? = null,
    val atualizado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: consulta_prenatal (DER)
 */
data class ConsultaPrenatal(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val gestacao_id: String = "",
    val titulo: String = "",
    val data: Timestamp = Timestamp.now(),
    val notas: String? = null,
    val concluida: Boolean = false,
    val criado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: mensagem_chat (DER)
 */
data class MensagemChat(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuario_id: String? = null,
    val sessao_id: String = UUID.randomUUID().toString(),
    val texto: String = "",
    val is_usuario: Boolean = true,
    val enviado_em: Timestamp = Timestamp.now()
)

/**
 * Tabela: categoria_artigo (DER)
 */
data class CategoriaArtigo(
    @DocumentId
    val id: String = "",
    val label: String = "",
    val icone: String = "",
    val cor: String = ""
)

/**
 * Tabela: artigo (DER)
 */
data class Artigo(
    @DocumentId
    val id: String = "",
    val categoria_id: String = "",
    val titulo: String = "",
    val resumo: String = "",
    val conteudo: String = "",
    val referencias: List<String> = emptyList(),
    val criado_em: Timestamp = Timestamp.now(),
    val atualizado_em: Timestamp = Timestamp.now(),
    // Helper para UI
    val categoria: String = ""
)

/**
 * Tabela: violentometro_nivel (DER)
 */
data class ViolentometroNivel(
    val nivel: Int = 1,
    val cor: String = "",
    val cor_fundo: String = "",
    val label: String = ""
)

/**
 * Tabela: violentometro_item (DER)
 */
data class ViolentometroItem(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val nivel: Int = 1,
    val descricao: String = "",
    val ordem: Int = 0
)
