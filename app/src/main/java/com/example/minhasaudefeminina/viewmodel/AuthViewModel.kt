package com.example.minhasaudefeminina.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.minhasaudefeminina.data.repository.AuthRepository
import com.example.minhasaudefeminina.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthEvent {
    ACCOUNT_UPDATED,
    PASSWORD_CHANGED,
    ACCOUNT_DELETED
}

data class AuthUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val user: Usuario? = null,
    val message: String? = null,
    val event: AuthEvent? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        repository.sessionUser
            .onEach { user ->
                _uiState.update { it.copy(isInitializing = false, user = user, isLoading = false) }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        isLoading = false,
                        message = error.message ?: "Não foi possível ler a sessão local."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun login(email: String, password: String) = runAction {
        repository.login(email, password.toCharArray())
    }

    fun createAccount(name: String, email: String, password: String) = runAction {
        repository.createAccount(name, email, password.toCharArray())
    }

    fun logout() = runAction { repository.logout() }

    fun updateAccount(name: String, email: String) {
        val userId = _uiState.value.user?.id ?: return
        runAction(AuthEvent.ACCOUNT_UPDATED) { repository.updateAccount(userId, name, email) }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        val userId = _uiState.value.user?.id ?: return
        runAction(AuthEvent.PASSWORD_CHANGED) {
            repository.changePassword(userId, currentPassword.toCharArray(), newPassword.toCharArray())
        }
    }

    fun deleteAccount(password: String) {
        val userId = _uiState.value.user?.id ?: return
        runAction(AuthEvent.ACCOUNT_DELETED) {
            repository.deleteAccount(userId, password.toCharArray())
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
    fun consumeEvent() = _uiState.update { it.copy(event = null) }

    private fun runAction(event: AuthEvent? = null, action: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, event = null) }
            runCatching { action() }
                .onSuccess { _uiState.update { it.copy(isLoading = false, event = event) } }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Não foi possível concluir a operação."
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(repository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(repository) }
        }
    }
}
