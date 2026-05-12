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
 * Registro de sintoma persistido no Firestore.
 * Todos os campos têm valor padrão para que o Firestore consiga
 * desserializar via toObject<RegistroSintoma>().
 *
 * Coleção: registrosSintomas
 */
data class RegistroSintoma(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "",
    val data: Timestamp = Timestamp.now(),
    val tipo: String = "",           // nome do enum SintomaTipo (ex: "SANGRAMENTO")
    val intensidade: Int = 1,        // 1 a 5
    val notas: String = "",
    val faseVidaNaData: String = "", // nome do enum FaseVida
    val alertaGerado: Boolean = false,
    val criadoEm: Timestamp = Timestamp.now()
)

/**
 * Perfil da usuária persistido no Firestore.
 * Coleção: usuarios
 */
data class Usuario(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val dataNascimento: String = "",  // formato ISO: "yyyy-MM-dd"
    val faseVida: String = FaseVida.IDADE_REPRODUTIVA.name,
    val duracaoCicloMedia: Int = 28,
    val duracaoMenstruacaoMedia: Int = 5,
    val criadoEm: Timestamp = Timestamp.now(),
    val ultimaAtualizacao: Timestamp = Timestamp.now()
)

/**
 * Alerta médico gerado automaticamente ao salvar um sintoma de alta intensidade.
 * Coleção: alertasGerados
 */
data class AlertaGerado(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "",
    val registroSintomaId: String = "",
    val mensagem: String = "",
    val tipoSintoma: String = "",
    val intensidade: Int = 0,
    val geradoEm: Timestamp = Timestamp.now(),
    val visualizado: Boolean = false
)

/**
 * Ciclo menstrual registrado.
 * Coleção: ciclos
 */
data class Ciclo(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val usuarioId: String = "",
    val dataInicio: String = "",  // formato ISO: "yyyy-MM-dd"
    val dataFim: String = "",
    val duracao: Int = 0,         // duração da menstruação em dias
    val duracaoCiclo: Int = 28,   // dias desde o início do ciclo anterior
    val notas: String = "",
    val criadoEm: Timestamp = Timestamp.now()
)
