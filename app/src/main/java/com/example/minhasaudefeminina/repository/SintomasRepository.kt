package com.example.minhasaudefeminina.repository

import com.example.minhasaudefeminina.model.AlertaGerado
import com.example.minhasaudefeminina.model.Ciclo
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável por toda comunicação com o Firestore.
 * Cada método é uma suspend fun ou retorna um Flow, mantendo
 * a lógica de negócio fora desta camada.
 */
class SintomasRepository {

    private val db = FirebaseFirestore.getInstance()

    // ── Coleções ──────────────────────────────────────────────────────────────
    private val colUsuarios         get() = db.collection("usuarios")
    private val colRegistros        get() = db.collection("registrosSintomas")
    private val colAlertas          get() = db.collection("alertasGerados")
    private val colCiclos           get() = db.collection("ciclos")

    // ── Usuário ───────────────────────────────────────────────────────────────

    /** Busca o perfil da usuária. Retorna null se ainda não existir. */
    suspend fun buscarUsuario(usuarioId: String): Usuario? {
        val doc = colUsuarios.document(usuarioId).get().await()
        return if (doc.exists()) doc.toObject(Usuario::class.java) else null
    }

    /** Cria ou atualiza o perfil da usuária. */
    suspend fun salvarUsuario(usuario: Usuario) {
        colUsuarios.document(usuario.id).set(usuario).await()
    }

    // ── Registros de Sintoma ──────────────────────────────────────────────────

    /** Persiste um registro de sintoma no Firestore. */
    suspend fun salvarRegistro(registro: RegistroSintoma) {
        colRegistros.document(registro.id).set(registro).await()
    }

    /**
     * Retorna um Flow com os registros da usuária em tempo real,
     * ordenados do mais recente para o mais antigo.
     */
    fun observarRegistros(usuarioId: String): Flow<List<RegistroSintoma>> = callbackFlow {
        val listener = colRegistros
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val registros = snapshot?.documents
                    ?.mapNotNull { it.toObject(RegistroSintoma::class.java) }
                    ?: emptyList()
                trySend(registros)
            }
        awaitClose { listener.remove() }
    }

    /** Busca registros de um tipo específico para a usuária. */
    suspend fun buscarRegistrosPorTipo(usuarioId: String, tipo: String): List<RegistroSintoma> {
        val snapshot = colRegistros
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("tipo", tipo)
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(RegistroSintoma::class.java) }
    }

    // ── Alertas ───────────────────────────────────────────────────────────────

    /** Persiste um alerta gerado automaticamente. */
    suspend fun salvarAlerta(alerta: AlertaGerado) {
        colAlertas.document(alerta.id).set(alerta).await()
    }

    /**
     * Retorna um Flow com os alertas não visualizados da usuária em tempo real.
     * Ideal para exibir um badge ou notificação na HomeScreen.
     */
    fun observarAlertasNaoVisualizados(usuarioId: String): Flow<List<AlertaGerado>> = callbackFlow {
        val listener = colAlertas
            .whereEqualTo("usuarioId", usuarioId)
            .whereEqualTo("visualizado", false)
            .orderBy("geradoEm", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val alertas = snapshot?.documents
                    ?.mapNotNull { it.toObject(AlertaGerado::class.java) }
                    ?: emptyList()
                trySend(alertas)
            }
        awaitClose { listener.remove() }
    }

    /** Marca um alerta como visualizado. */
    suspend fun marcarAlertaVisualizado(alertaId: String) {
        colAlertas.document(alertaId).update("visualizado", true).await()
    }

    // ── Ciclos ────────────────────────────────────────────────────────────────

    /** Persiste um ciclo menstrual. */
    suspend fun salvarCiclo(ciclo: Ciclo) {
        colCiclos.document(ciclo.id).set(ciclo).await()
    }

    /** Retorna os ciclos da usuária ordenados do mais recente. */
    suspend fun buscarCiclos(usuarioId: String): List<Ciclo> {
        val snapshot = colCiclos
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("dataInicio", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(Ciclo::class.java) }
    }
}
