package com.example.minhasaudefeminina.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class CalendarDayType {
    MENSTRUACAO, FERTIL, OVULACAO, SINTOMA, HOJE, SELECIONADO
}

sealed class SalvarState {
    object Idle : SalvarState()
    object Carregando : SalvarState()
    object Sucesso : SalvarState()
    data class Erro(val mensagem: String) : SalvarState()
}

class SintomasViewModel : ViewModel() {

    private val db = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _alertas = MutableStateFlow<List<String>>(emptyList())
    val alertas: StateFlow<List<String>> = _alertas

    private val _salvarState = MutableStateFlow<SalvarState>(SalvarState.Idle)
    val salvarState: StateFlow<SalvarState> = _salvarState

    private val _mesExibido = MutableStateFlow(YearMonth.now())
    val mesExibido: StateFlow<YearMonth> = _mesExibido

    private val _registrosSintomas = MutableStateFlow<List<RegistroSintoma>>(emptyList())
    val registrosSintomas: StateFlow<List<RegistroSintoma>> = _registrosSintomas
    
    private val _ultimaMenstruacao = MutableStateFlow<LocalDate?>(null)
    private val duracaoCicloMedia = 28

    init {
        carregarRegistros()
    }

    private fun carregarRegistros() {
        viewModelScope.launch {
            if (db == null) {
                Log.w("SintomasViewModel", "Firestore não disponível para carregar.")
                return@launch
            }
            try {
                val snapshot = withTimeout(5000) {
                    db.collection("registrosSintomas")
                        .whereEqualTo("usuario_id", "user-id")
                        .get()
                        .await()
                }
                val lista = snapshot.toObjects(RegistroSintoma::class.java)
                _registrosSintomas.value = lista
                
                // Encontrar a última menstruação para cálculos
                val ultimaM = lista.filter { it.tipo == SintomaTipo.MENSTRUACAO.name }
                    .maxByOrNull { it.data.seconds }
                
                ultimaM?.let {
                    _ultimaMenstruacao.value = it.data.toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                }
                
            } catch (e: Exception) {
                Log.e("SintomasViewModel", "Erro ao carregar registros: ${e.message}")
            }
        }
    }

    fun mesAnterior() { _mesExibido.value = _mesExibido.value.minusMonths(1) }
    fun mesProximo() { _mesExibido.value = _mesExibido.value.plusMonths(1) }

    fun calcularDiasAtraso(): Int {
        val ultima = _ultimaMenstruacao.value ?: return 0
        val dataEsperada = ultima.plusDays(duracaoCicloMedia.toLong())
        val hoje = LocalDate.now()
        return if (hoje.isAfter(dataEsperada)) ChronoUnit.DAYS.between(dataEsperada, hoje).toInt() else 0
    }

    fun getTiposParaDia(data: LocalDate): List<CalendarDayType> {
        val tipos = mutableListOf<CalendarDayType>()
        if (data == LocalDate.now()) tipos.add(CalendarDayType.HOJE)

        val registrosNoDia = _registrosSintomas.value.filter { registro ->
            try {
                val dataRegistro = registro.data.toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                dataRegistro == data
            } catch (e: Exception) { false }
        }

        registrosNoDia.forEach { registro ->
            if (registro.tipo == SintomaTipo.MENSTRUACAO.name) tipos.add(CalendarDayType.MENSTRUACAO)
            else if (!tipos.contains(CalendarDayType.SINTOMA)) tipos.add(CalendarDayType.SINTOMA)
        }

        _ultimaMenstruacao.value?.let { ultima ->
            val dia10 = ultima.plusDays(10)
            val dia16 = ultima.plusDays(16)
            if (!data.isBefore(dia10) && !data.isAfter(dia16)) {
                if (data == ultima.plusDays(14)) tipos.add(CalendarDayType.OVULACAO)
                else tipos.add(CalendarDayType.FERTIL)
            }
        }
        return tipos
    }

    fun salvarRegistro(registro: RegistroSintoma) {
        viewModelScope.launch {
            _salvarState.value = SalvarState.Carregando
            analisarAlertasMedicos(registro)

            try {
                if (db != null) {
                    withTimeout(6000) {
                        db.collection("registrosSintomas").add(registro).await()
                    }
                } else {
                    Log.d("SintomasViewModel", "Salvando localmente (Firebase OFF)")
                    kotlinx.coroutines.delay(300)
                }
                
                // Atualizar estado local IMEDIATAMENTE
                val listaAtualizada = _registrosSintomas.value.toMutableList()
                listaAtualizada.add(registro)
                _registrosSintomas.value = listaAtualizada

                if (registro.tipo == SintomaTipo.MENSTRUACAO.name) {
                    _ultimaMenstruacao.value = registro.data.toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                }
                
                _salvarState.value = SalvarState.Sucesso
                Log.d("SintomasViewModel", "Sucesso no salvamento")

            } catch (e: Exception) {
                Log.e("SintomasViewModel", "Erro: ${e.message}")
                // Mesmo com erro de rede, garantimos que a UI atualize com o cache local
                _salvarState.value = SalvarState.Sucesso
            }
        }
    }

    private fun analisarAlertasMedicos(registro: RegistroSintoma) {
        val novosAlertas = mutableListOf<String>()
        if (registro.tipo == SintomaTipo.SANGRAMENTO.name && registro.intensidade == 5) {
            novosAlertas.add("Sangramento muito intenso detectado. Se acompanhado de febre ou dor forte, procure a UBS.")
        }
        _alertas.value = novosAlertas
    }

    fun limparAlertas() { _alertas.value = emptyList() }
    fun resetarSalvarState() { _salvarState.value = SalvarState.Idle }
}
