package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.repository.ProfileRepository
import com.example.minhasaudefeminina.data.repository.SymptomRepository
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.model.PerfilUsuario
import com.example.minhasaudefeminina.model.RegistroSintoma
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val profileRepository: ProfileRepository,
    symptomRepository: SymptomRepository,
    private val userId: String
) : ViewModel() {
    val profile: StateFlow<PerfilUsuario> = profileRepository.observeProfile(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUsuario(userId))

    val registrosSintomas: StateFlow<List<RegistroSintoma>> = symptomRepository.observeRecords(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setFaseVida(fase: FaseVida) = save(profile.value.copy(faseVida = fase))
    fun setGestante(value: Boolean) = save(profile.value.copy(estaGestante = value))
    fun setPhotoUri(uri: String?) = save(profile.value.copy(fotoUri = uri))
    fun setPapanicolauDate(timestamp: Long?) = save(profile.value.copy(dataPapanicolau = timestamp))
    fun setMamografiaDate(timestamp: Long?) = save(profile.value.copy(dataMamografia = timestamp))

    fun consumeMessage() {
        _message.value = null
    }

    private fun save(updated: PerfilUsuario) {
        viewModelScope.launch {
            runCatching { profileRepository.saveProfile(updated) }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar o perfil." }
        }
    }

    companion object {
        fun factory(
            profileRepository: ProfileRepository,
            symptomRepository: SymptomRepository,
            userId: String
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { PerfilViewModel(profileRepository, symptomRepository, userId) }
        }
    }
}
