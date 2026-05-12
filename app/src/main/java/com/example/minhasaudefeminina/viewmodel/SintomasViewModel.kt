package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.example.minhasaudefeminina.model.SintomaTipo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SintomasViewModel : ViewModel() {

    private val _alertas = MutableStateFlow<List<String>>(emptyList())
    val alertas: StateFlow<List<String>> = _alertas

    fun salvarRegistro(registro: RegistroSintoma) {
        val novosAlertas = mutableListOf<String>()

        // Regra: Sangramento Intenso
        if (registro.tipo == SintomaTipo.SANGRAMENTO && registro.intensidade == 5) {
            novosAlertas.add("Sangramento muito intenso detectado. Se acompanhado de febre ou dor forte, procure a UBS.")
        }

        // Regra: Sintomas Urinários
        if (registro.tipo == SintomaTipo.SINTOMA_URINARIO && registro.intensidade >= 4) {
            novosAlertas.add("Sintomas urinários intensos. É recomendável uma consulta preventiva na UBS.")
        }

        // Regra: Fogachos/Suor Noturno
        if ((registro.tipo == SintomaTipo.FOGACHOS || registro.tipo == SintomaTipo.SUOR_NOTURNO) && registro.intensidade >= 4) {
            novosAlertas.add("Sintomas de calor intenso detectados. Converse com um profissional de saúde sobre opções de alívio.")
        }

        _alertas.value = novosAlertas
        
        // Aqui integraria com Firebase futuramente
        println("Registro salvo: ${registro.tipo} com intensidade ${registro.intensidade}")
    }

    fun limparAlertas() {
        _alertas.value = emptyList()
    }

    fun validarDuracaoCiclo(duracao: Int): Boolean {
        return duracao in 15..60
    }
}
