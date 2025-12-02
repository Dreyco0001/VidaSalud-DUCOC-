package com.example.vidasalud.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.repository.FeedRepository
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestoreException

class FeedViewModel(
    private val repo: FeedRepository = FeedRepository()
) : ViewModel() {

    private val _comentarios = MutableLiveData<List<Comentario>>(emptyList())
    val comentarios: LiveData<List<Comentario>> get() = _comentarios

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    init {
        escucharCambios()
    }

    private fun escucharCambios() {
        try {
            repo.escucharComentarios { lista ->
                _comentarios.postValue(lista)
            }
        } catch (e: Exception) {
            manejarFirebaseError(e)
        }
    }

    // --------------------------------------------------------------
    // 🔥 MANEJADOR COMPLETO DE ERRORES FIREBASE
    // --------------------------------------------------------------
    private fun manejarFirebaseError(e: Exception, userIdEnviado: String = "") {
        val authUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "No autenticado"
        val msgBase = e.message ?: "Error desconocido en Firebase."

        // Construimos el mensaje final
        val msg = """
🔥 Firebase bloqueó la operación

🔎 Mensaje original:
$msgBase

⚠ Comparación de IDs requerida por Firestore:
request.resource.data.userId == request.auth.uid

Ejemplo:
• userId enviado desde tu app: $userIdEnviado
• auth.uid real del usuario: $authUid

Si NO coinciden → Firestore devuelve PERMISSION_DENIED.
""".trimIndent()

        _error.postValue(msg)
        e.printStackTrace()
    }


    // --------------------------------------------------------------
    // CREAR COMENTARIO
    // --------------------------------------------------------------
    fun enviarMensaje(
        texto: String,
        userId: String,
        userName: String,
        fotoUrl: String = ""
    ) {
        if (texto.length > 250) {
            _error.value = "Máximo 250 caracteres."
            return
        }

        viewModelScope.launch {
            try {
                val comentario = Comentario(
                    id = "",
                    userId = userId,
                    userName = userName,
                    mensaje = texto,
                    fotoUrl = fotoUrl,
                    timestamp = System.currentTimeMillis()
                )

                val result = repo.enviarComentario(comentario)

                if (result.isFailure) {
                    manejarFirebaseError(result.exceptionOrNull()!! as Exception)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // EDITAR COMENTARIO
    // --------------------------------------------------------------
    fun editarComentario(
        comentarioId: String,
        nuevoMensaje: String,
        userId: String,
        isAdmin: Boolean = false
    ) {
        if (nuevoMensaje.length > 250) {
            _error.value = "Máximo 250 caracteres."
            return
        }

        viewModelScope.launch {
            try {
                val result = repo.editarComentario(comentarioId, nuevoMensaje, userId, isAdmin)
                if (result.isFailure) {
                    manejarFirebaseError(result.exceptionOrNull()!! as Exception)
                }
            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // ELIMINAR COMENTARIO
    // --------------------------------------------------------------
    fun eliminarComentario(
        comentarioId: String,
        userId: String,
        isAdmin: Boolean
    ) {
        viewModelScope.launch {
            try {
                val result = repo.eliminarComentario(comentarioId, userId, isAdmin)
                if (result.isFailure) {
                    manejarFirebaseError(result.exceptionOrNull()!! as Exception)
                }
            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // LIKE / UNLIKE
    // --------------------------------------------------------------
    fun toggleLike(comentarioId: String, userId: String) {
        viewModelScope.launch {
            try {
                val likes = repo.obtenerLikes(comentarioId)
                val existe = likes.any { it.userId == userId }

                if (!existe) {
                    val r = repo.agregarLike(comentarioId, userId)
                    if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception)
                } else {
                    val r = repo.quitarLike(comentarioId, userId)
                    if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    suspend fun getLikes(comentarioId: String): Int {
        return try {
            repo.obtenerLikes(comentarioId).size
        } catch (e: Exception) {
            manejarFirebaseError(e)
            0
        }
    }
}
