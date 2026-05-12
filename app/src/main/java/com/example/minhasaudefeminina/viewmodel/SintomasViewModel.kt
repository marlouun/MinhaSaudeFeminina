package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minhasaudefeminina.model.AlertaGerado
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.example.minhasaudefeminina.repository.SintomasRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Estados possíveis de uma operação de salvamento.
 */
sealed class SalvarState {
    object Idle : SalvarState()
    object Carregando : SalvarState()
    object Sucesso : SalvarState()
    data class Erro(val mensagem: String) : SalvarState()
}

class SintomasViewModel(
    private val repository: SintomasRepository = SintomasRepository()
) : ViewModel() {

    // ── Alertas exibidos como Snackbar na tela de registro ────────────────────
    private val _alertas = MutableStateFlow<List<String>>(emptyList())
    val alertas: StateFlow<List<String>> = _alertas

    // ── Estado do botão Salvar ────────────────────────────────────────────────
    private val _salvarState = MutableStateFlow<SalvarState>(SalvarState.Idle)
    val salvarState: StateFlow<SalvarState> = _salvarState

    // ── Lista de registros observada em tempo real ────────────────────────────
    private val _registros = MutableStateFlow<List<RegistroSintoma>>(emptyList())
    val registros: StateFlow<List<RegistroSintoma>> = _registros

    // ── Alertas não visualizados (badge na HomeScreen) ────────────────────────
    private val _alertasNaoVisualizados = MutableStateFlow<List<AlertaGerado>>(emptyList())
    val alertasNaoVisualizados: StateFlow<List<AlertaGerado>> = _alertasNaoVisualizados

    /**
     * Inicia a observação em tempo real dos registros e alertas da usuária.
     * Deve ser chamado assim que o usuarioId estiver disponível (após login).
     */
    fun iniciarObservacao(usuarioId: String) {
        viewModelScope.launch {
            repository.observarRegistros(usuarioId)
                .catch { e -> _salvarState.value = SalvarState.Erro(e.message ?: "Erro ao carregar registros") }
                .collect { _registros.value = it }
        }
        viewModelScope.launch {
            repository.observarAlertasNaoVisualizados(usuarioId)
                .catch { /* silencia — alertas são secundários */ }
                .collect { _alertasNaoVisualizados.value = it }
        }
    }

    /**
     * Valida, aplica regras de alerta médico e persiste o registro no Firestore.
     * [faseVidaAtual] é a fase de vida da usuária no momento do registro.
     */
    fun salvarRegistro(
        registro: RegistroSintoma,
        faseVidaAtual: FaseVida = FaseVida.IDADE_REPRODUTIVA
    ) {
        _salvarState.value = SalvarState.Carregando

        val mensagensAlerta = avaliarAlertas(registro)
        val registroCompleto = registro.copy(
            faseVidaNaData = faseVidaAtual.name,
            alertaGerado = mensagensAlerta.isNotEmpty(),
            criadoEm = Timestamp.now()
        )

        viewModelScope.launch {
            try {
                repository.salvarRegistro(registroCompleto)

                // Persiste cada alerta gerado como documento separado
                mensagensAlerta.forEach { mensagem ->
                    val alerta = AlertaGerado(
                        id = UUID.randomUUID().toString(),
                        usuarioId = registro.usuarioId,
                        registroSintomaId = registro.id,
                        mensagem = mensagem,
                        tipoSintoma = registro.tipo,
                        intensidade = registro.intensidade,
                        geradoEm = Timestamp.now()
                    )
                    repository.salvarAlerta(alerta)
                }

                _alertas.value = mensagensAlerta
                _salvarState.value = SalvarState.Sucesso
            } catch (e: Exception) {
                _salvarState.value = SalvarState.Erro(e.message ?: "Erro ao salvar registro")
            }
        }
    }

    /** Marca um alerta como visualizado no Firestore. */
    fun marcarAlertaVisualizado(alertaId: String) {
        viewModelScope.launch {
            try {
                repository.marcarAlertaVisualizado(alertaId)
            } catch (_: Exception) { /* silencia */ }
        }
    }

    fun limparAlertas() {
        _alertas.value = emptyList()
    }

    fun resetarSalvarState() {
        _salvarState.value = SalvarState.Idle
    }

    fun validarDuracaoCiclo(duracao: Int): Boolean = duracao in 15..60

    // ── Regras de alerta médico ───────────────────────────────────────────────

    private fun avaliarAlertas(registro: RegistroSintoma): List<String> {
        val tipo = runCatching { SintomaTipo.valueOf(registro.tipo) }.getOrNull()
            ?: return emptyList()

        val alertas = mutableListOf<String>()

        if (tipo == SintomaTipo.SANGRAMENTO && registro.intensidade == 5) {
            alertas.add("Sangramento muito intenso detectado. Se acompanhado de febre ou dor forte, procure a UBS.")
        }
        if (tipo == SintomaTipo.SINTOMA_URINARIO && registro.intensidade >= 4) {
            alertas.add("Sintomas urinários intensos. É recomendável uma consulta preventiva na UBS.")
        }
        if ((tipo == SintomaTipo.FOGACHOS || tipo == SintomaTipo.SUOR_NOTURNO) && registro.intensidade >= 4) {
            alertas.add("Sintomas de calor intenso detectados. Converse com um profissional de saúde sobre opções de alívio.")
        }

        return alertas
    }
}
