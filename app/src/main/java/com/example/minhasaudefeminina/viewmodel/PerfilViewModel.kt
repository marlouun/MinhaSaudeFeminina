package com.example.minhasaudefeminina.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.minhasaudefeminina.model.FaseVida
import com.example.minhasaudefeminina.model.PerfilUsuario
import com.example.minhasaudefeminina.model.RegistroSintoma
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilViewModel : ViewModel() {
    private val dbUrl = "https://device-streaming-9c4db877-default-rtdb.firebaseio.com/"
    private val auth = try { FirebaseAuth.getInstance() } catch(e: Exception) { null }
    private val db = try { 
        FirebaseDatabase.getInstance(dbUrl).reference 
    } catch (e: Exception) { null }

    private val _faseVida = MutableStateFlow(FaseVida.IDADE_REPRODUTIVA)
    val faseVida: StateFlow<FaseVida> = _faseVida

    private val _isGestante = MutableStateFlow(false)
    val isGestante: StateFlow<Boolean> = _isGestante

    private val _dataPapanicolau = MutableStateFlow("")
    val dataPapanicolau: StateFlow<String> = _dataPapanicolau

    private val _dataMamografia = MutableStateFlow("")
    val dataMamografia: StateFlow<String> = _dataMamografia

    private val _registrosSintomas = MutableStateFlow<List<RegistroSintoma>>(emptyList())
    val registrosSintomas: StateFlow<List<RegistroSintoma>> = _registrosSintomas

    private val _photoUri = MutableStateFlow<android.net.Uri?>(null)
    val photoUri: StateFlow<android.net.Uri?> = _photoUri

    init {
        carregarPerfil()
    }

    private fun carregarPerfil() {
        val userId = auth?.currentUser?.uid ?: return
        
        // Perfil
        db?.child("usuarios")?.child(userId)?.child("perfil")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val perfil = snapshot.getValue(PerfilUsuario::class.java)
                        perfil?.let {
                            _faseVida.value = try { FaseVida.valueOf(it.fase_vida) } catch (e: Exception) { FaseVida.IDADE_REPRODUTIVA }
                        }
                    } catch (e: Exception) {
                        Log.e("PerfilViewModel", "Erro perfil: ${e.message}")
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
            
        // Gestante
        db?.child("usuarios")?.child(userId)?.child("dados_gestacao")?.child("esta_gestante")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isGestante.value = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // Sintomas para o relatório
        db?.child("usuarios")?.child(userId)?.child("registros_sintomas")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<RegistroSintoma>()
                    snapshot.children.forEach { child ->
                        child.getValue(RegistroSintoma::class.java)?.let { lista.add(it) }
                    }
                    _registrosSintomas.value = lista
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun setFaseVida(fase: FaseVida) {
        _faseVida.value = fase
        salvarPerfil()
    }

    fun setGestante(value: Boolean) {
        _isGestante.value = value
        val userId = auth?.currentUser?.uid ?: return
        db?.child("usuarios")?.child(userId)?.child("dados_gestacao")?.child("esta_gestante")?.setValue(value)
    }

    fun setDataPapanicolau(data: String) { _dataPapanicolau.value = data }
    fun setDataMamografia(data: String) { _dataMamografia.value = data }
    fun setPhotoUri(uri: android.net.Uri) { _photoUri.value = uri }

    private fun salvarPerfil() {
        val userId = auth?.currentUser?.uid ?: return
        val perfil = mapOf("fase_vida" to _faseVida.value.name, "usuario_id" to userId)
        db?.child("usuarios")?.child(userId)?.child("perfil")?.updateChildren(perfil)
    }

    fun signOut() { auth?.signOut() }
}
