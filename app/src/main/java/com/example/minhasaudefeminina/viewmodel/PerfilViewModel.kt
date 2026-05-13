package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minhasaudefeminina.model.FaseVida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilViewModel : ViewModel() {
    private val _faseVida = MutableStateFlow(FaseVida.IDADE_REPRODUTIVA)
    val faseVida: StateFlow<FaseVida> = _faseVida

    private val _isGestante = MutableStateFlow(false)
    val isGestante: StateFlow<Boolean> = _isGestante

    private val _dataPapanicolau = MutableStateFlow("")
    val dataPapanicolau: StateFlow<String> = _dataPapanicolau

    private val _dataMamografia = MutableStateFlow("")
    val dataMamografia: StateFlow<String> = _dataMamografia

    fun setFaseVida(fase: FaseVida) {
        _faseVida.value = fase
    }

    fun setGestante(value: Boolean) {
        _isGestante.value = value
    }

    fun setDataPapanicolau(data: String) {
        _dataPapanicolau.value = data
    }

    fun setDataMamografia(data: String) {
        _dataMamografia.value = data
    }
}
