package com.example.vidasalud.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.repository.FeedRepository
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repo: FeedRepository = FeedRepository()
) : ViewModel() {

    private val _comentarios = MutableLiveData<List<Comentario>>(emptyList())
    val comentarios: LiveData<List<Comentario>> get() = _comentarios

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        escucharCambios()
    }

    private fun escucharCambios() {
        repo.escucharComentarios { lista ->
            _comentarios.postValue(lista)
        }
    }

    // --------------------------------------------------------------
    // ENVIAR COMENTARIO
    // --------------------------------------------------------------
    fun enviarMensaje(
        texto: String,
        userId: String,
        userName: String
    ) {
        if (texto.length > 250) {
            _error.value = "Máximo 250 caracteres"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val fotoRemota = repo.obtenerFotoUsuario(userId)
            val fotoFinal = fotoRemota ?: "default_profile"

            val comentario = Comentario(
                userId = userId,
                userName = userName,
                fotoUrl = fotoFinal,
                mensaje = texto,
                timestamp = System.currentTimeMillis()
            )

            val result = repo.enviarComentario(comentario)

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    // --------------------------------------------------------------
    // EDITAR COMENTARIO (CON REGLAS)
    // --------------------------------------------------------------
    fun editarComentario(
        comentarioId: String,
        nuevoTexto: String,
        userId: String,
        isAdmin: Boolean
    ) {
        if (nuevoTexto.length > 250) {
            _error.value = "Máximo 250 caracteres"
            return
        }

        viewModelScope.launch {
            val comentario = repo.obtenerComentarioPorId(comentarioId)
            if (comentario == null) {
                _error.value = "Comentario no encontrado"
                return@launch
            }

            val esCreador = comentario.userId == userId
            val tiempoPasado = System.currentTimeMillis() - comentario.timestamp
            val limiteEdicion = 10 * 60 * 1000L // 10 min

            if (!isAdmin) {
                if (!esCreador) {
                    _error.value = "Solo el creador del comentario puede editarlo"
                    return@launch
                }

                if (tiempoPasado > limiteEdicion) {
                    _error.value = "Solo puedes editar dentro de los primeros 10 minutos"
                    return@launch
                }
            }

            val result = repo.editarComentario(
                comentarioId,
                nuevoTexto,
                userId,
                isAdmin
            )

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
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
            val result = repo.eliminarComentario(
                comentarioId,
                userId,
                isAdmin
            )

            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
        }
    }

    // --------------------------------------------------------------
    // LIKE / UNLIKE
    // --------------------------------------------------------------
    fun toggleLike(comentarioId: String, userId: String) {
        viewModelScope.launch {
            val likes = repo.obtenerLikes(comentarioId)
            val likeExistente = likes.find { it.userId == userId }

            if (likeExistente == null) {
                repo.agregarLike(comentarioId, userId)
            } else {
                repo.quitarLike(comentarioId, likeExistente.id)
            }
        }
    }

    suspend fun getLikes(comentarioId: String): Int {
        return repo.obtenerLikes(comentarioId).size
    }
}
