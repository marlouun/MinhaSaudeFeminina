package com.example.minhasaudefeminina.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordChanged : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth? = try { 
        FirebaseAuth.getInstance() 
    } catch (e: Exception) { 
        Log.e("AuthViewModel", "Erro ao inicializar FirebaseAuth: ${e.message}")
        null 
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated()
        }
    }

    init {
        try {
            auth?.addAuthStateListener(authListener)
            if (auth == null) {
                _authState.value = AuthState.Unauthenticated("Serviço de autenticação indisponível.")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Erro no init: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            auth?.removeAuthStateListener(authListener)
        } catch (e: Exception) {}
    }

    fun checkAuthStatus() {
        val currentUser = auth?.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated()
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos.")
            return
        }

        _authState.value = AuthState.Loading
        auth?.signInWithEmailAndPassword(email, pass)
            ?.addOnSuccessListener {
                checkAuthStatus()
            }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Erro ao entrar.")
            } ?: run {
                _authState.value = AuthState.Error("Serviço indisponível.")
            }
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos.")
            return
        }

        _authState.value = AuthState.Loading
        auth?.createUserWithEmailAndPassword(email, pass)
            ?.addOnSuccessListener {
                checkAuthStatus()
            }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Erro ao cadastrar.")
            } ?: run {
                _authState.value = AuthState.Error("Serviço indisponível.")
            }
    }

    fun signOut() {
        auth?.signOut()
        _authState.value = AuthState.Unauthenticated()
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnSuccessListener { checkAuthStatus() }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Erro ao entrar com Google.")
            }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun changePassword(currentPass: String, newPass: String) {
        val user = auth?.currentUser ?: return
        val email = user.email ?: return

        _authState.value = AuthState.Loading
        
        val credential = EmailAuthProvider.getCredential(email, currentPass)
        user.reauthenticate(credential).addOnSuccessListener {
            user.updatePassword(newPass).addOnSuccessListener {
                _authState.value = AuthState.PasswordChanged
            }.addOnFailureListener {
                _authState.value = AuthState.Error("Erro ao atualizar: ${it.localizedMessage}")
            }
        }.addOnFailureListener {
            _authState.value = AuthState.Error("Senha atual incorreta.")
        }
    }
}
