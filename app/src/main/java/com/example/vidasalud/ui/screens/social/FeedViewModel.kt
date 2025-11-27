package com.example.vidasalud.ui.screens.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val comentariosRepository = ComentariosRepository()
    private val authRepository = AuthRepository()

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    private var userName = "Anónimo"

    private val planId = "plan_comunidad"

    init {
        viewModelScope.launch {
            authRepository.getCurrentUserData()?.let { user ->
                userName = user.nombre
            }

            val comentariosExistentes = comentariosRepository.obtenerComentarios(planId)
            if (comentariosExistentes.isEmpty()) {
                comentariosRepository.agregarComentariosDeEjemplo(planId)
            }
            comentariosRepository.escucharComentarios(planId) {
                _comentarios.value = it
            }
        }
    }

    fun onMensajeChanged(nuevoMensaje: String) {
        _mensaje.value = nuevoMensaje
    }

    fun enviarMensaje() {
        val user = FirebaseAuth.getInstance().currentUser
        val mensajeActual = _mensaje.value

        if (user != null && mensajeActual.isNotBlank()) {
            val timestamp = System.currentTimeMillis()

            val nuevoComentario = Comentario(
                userId = user.uid,
                userName = this.userName,
                mensaje = mensajeActual,
                timestamp = timestamp
            )

            _comentarios.update { it + nuevoComentario }

            _mensaje.value = ""

            viewModelScope.launch {
                comentariosRepository.agregarComentario(
                    planId = planId,
                    userId = user.uid,
                    userName = userName,
                    mensaje = mensajeActual,
                    timestamp = timestamp
                )
            }
        }
    }
}
