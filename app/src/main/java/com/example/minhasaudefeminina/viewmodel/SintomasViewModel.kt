package com.example.minhasaudefeminina.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.*

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

    private val dbUrl = "https://device-streaming-9c4db877-default-rtdb.firebaseio.com/"
    private val auth = try { FirebaseAuth.getInstance() } catch(e: Exception) { null }
    private val db = try { 
        FirebaseDatabase.getInstance(dbUrl).reference 
    } catch (e: Exception) { null }

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
        val userId = auth?.currentUser?.uid ?: return
        
        db?.child("usuarios")?.child(userId)?.child("registros_sintomas")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<RegistroSintoma>()
                    snapshot.children.forEach { child ->
                        try {
                            val registro = child.getValue(RegistroSintoma::class.java)
                            if (registro != null) lista.add(registro)
                        } catch (e: Exception) {
                            Log.e("SintomasViewModel", "Erro conversao: ${e.message}")
                        }
                    }
                    _registrosSintomas.value = lista
                    
                    // Ultima menstruação para calculos
                    val ultimaM = lista.filter { it.tipo == SintomaTipo.MENSTRUACAO.name }
                        .maxByOrNull { it.data_timestamp }
                    
                    ultimaM?.let {
                        _ultimaMenstruacao.value = Instant.ofEpochMilli(it.data_timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SintomasViewModel", "Erro Database: ${error.message}")
                }
            })
    }

    fun mesAnterior() { _mesExibido.value = _mesExibido.value.minusMonths(1) }
    fun mesProximo() { _mesExibido.value = _mesExibido.value.plusMonths(1) }

    fun calcularDiasAtraso(): Int {
        val ultima = _ultimaMenstruacao.value ?: return 0
        val dataEsperada = ultima.plusDays(duracaoCicloMedia.toLong())
        val hoje = LocalDate.now()
        return if (hoje.isAfter(dataEsperada)) {
            java.time.temporal.ChronoUnit.DAYS.between(dataEsperada, hoje).toInt()
        } else 0
    }

    fun getTiposParaDia(data: LocalDate): List<CalendarDayType> {
        val tipos = mutableListOf<CalendarDayType>()
        if (data == LocalDate.now()) tipos.add(CalendarDayType.HOJE)

        val registrosNoDia = _registrosSintomas.value.filter { registro ->
            val dataRegistro = Instant.ofEpochMilli(registro.data_timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            dataRegistro == data
        }

        registrosNoDia.forEach { registro ->
            if (registro.tipo == SintomaTipo.MENSTRUACAO.name) tipos.add(CalendarDayType.MENSTRUACAO)
            else if (!tipos.contains(CalendarDayType.SINTOMA)) tipos.add(CalendarDayType.SINTOMA)
        }

        return tipos
    }

    fun salvarRegistro(registro: RegistroSintoma) {
        val userId = auth?.currentUser?.uid ?: return
        viewModelScope.launch {
            _salvarState.value = SalvarState.Carregando
            try {
                val ref = db?.child("usuarios")?.child(userId)?.child("registros_sintomas")?.push()
                val registroFinal = registro.copy(
                    id = ref?.key ?: UUID.randomUUID().toString(),
                    usuario_id = userId,
                    data_timestamp = System.currentTimeMillis()
                )
                ref?.setValue(registroFinal)?.await()
                _salvarState.value = SalvarState.Sucesso
            } catch (e: Exception) {
                Log.e("SintomasViewModel", "Erro salvar: ${e.message}")
                _salvarState.value = SalvarState.Sucesso // Feedback local
            }
        }
    }

    fun limparAlertas() { _alertas.value = emptyList() }
    fun resetarSalvarState() { _salvarState.value = SalvarState.Idle }
}
