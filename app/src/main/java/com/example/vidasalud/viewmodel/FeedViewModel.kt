package com.example.vidasalud.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.repository.FeedRepository
import kotlinx.coroutines.launch
import java.io.IOException
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
    // 🔥 MANEJADOR REFORZADO DE ERRORES FIREBASE
    // --------------------------------------------------------------
    private fun manejarFirebaseError(e: Exception) {

        // Mensaje base
        var msg = e.message ?: "Error desconocido en Firebase."

        // -----------------------------
        // 💥 ERRORES DE PERMISOS
        // -----------------------------
        if (msg.contains("PERMISSION_DENIED", true) ||
            msg.contains("insufficient permissions", true)
        ) {
            msg = "PERMISOS DENEGADOS — Firebase bloqueó la operación."
        }

        // -----------------------------
        // 💥 ERRORES POR userId (cuando Firestore valida request.auth.uid)
        // -----------------------------
        if (msg.contains("userId", true) ||
            msg.contains("uid", true) ||
            msg.contains("Missing or insufficient permissions", true)
        ) {

            msg += "\n\n⚠ Posible error por ID.\n" +
                    "Comparación esperada por Firebase:\n" +
                    "→ request.resource.data.userId == request.auth.uid\n\n" +
                    "Ejemplos:\n" +
                    "• userId enviado desde la app: (revísalo en el objeto Comentario)\n" +
                    "• userId autenticado en Firebase: FirebaseAuth.getInstance().currentUser?.uid\n\n" +
                    "Si NO coinciden → comentario no se crea."
        }

        // Manda el mensaje final
        _error.postValue(msg)
        e.printStackTrace()
    }


    // --------------------------------------------------------------
    // ENVIAR
    // --------------------------------------------------------------
    fun enviarMensaje(
        texto: String,
        userId: String,
        userName: String,
        fotoUrl: String = ""
    ) {
        if (texto.length > 250) {
            _error.value = "Máximo 250 caracteres"
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
                    manejarFirebaseError((result.exceptionOrNull() ?: Exception("Fallo desconocido.")) as Exception)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // EDITAR
    // --------------------------------------------------------------
    fun editarComentario(
        comentarioId: String,
        nuevoMensaje: String,
        userId: String,
        isAdmin: Boolean = false
    ) {
        if (nuevoMensaje.length > 250) {
            _error.value = "Máximo 250 caracteres"
            return
        }

        viewModelScope.launch {
            try {
                val result = repo.editarComentario(comentarioId, nuevoMensaje, userId, isAdmin)

                if (result.isFailure) {
                    manejarFirebaseError((result.exceptionOrNull() ?: Exception("Error al editar.")) as Exception)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // ELIMINAR
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
                    manejarFirebaseError((result.exceptionOrNull() ?: Exception("Error al eliminar.")) as Exception)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }

    // --------------------------------------------------------------
    // LIKE
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
