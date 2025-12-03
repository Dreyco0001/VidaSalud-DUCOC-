package com.example.vidasalud.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.example.vidasalud.repository.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class FeedViewModel(
    val repo: FeedRepository = FeedRepository()
) : ViewModel() {

    // --------------------------------------------------------------
    //  LIVE DATA
    // --------------------------------------------------------------
    private val _comentarios = MutableLiveData<List<Comentario>>(emptyList())
    val comentarios: LiveData<List<Comentario>> get() = _comentarios

    private val likesMap = mutableMapOf<String, Int>()
    private val _likesLive = MutableLiveData<Map<String, Int>>()
    val likesLive: LiveData<Map<String, Int>> get() = _likesLive

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error


    // --------------------------------------------------------------
    //  LISTENERES DE LIKES
    // --------------------------------------------------------------
    private val likeListeners = mutableMapOf<String, ListenerRegistration>()


    init {
        escucharComentarios()
    }


    // --------------------------------------------------------------
    // ESCUCHAR COMENTARIOS EN TIEMPO REAL
    // --------------------------------------------------------------
    private fun escucharComentarios() {
        repo.escucharComentarios { lista ->

            _comentarios.postValue(lista)

            lista.forEach { comentario ->

                // evita duplicar listeners
                if (!likeListeners.containsKey(comentario.id)) {
                    escucharLikesComentario(comentario.id)
                }
            }
        }
    }

    // --------------------------------------------------------------
    // ESCUCHAR LIKES EN TIEMPO REAL POR COMENTARIO
    // --------------------------------------------------------------
    private fun escucharLikesComentario(comentarioId: String) {

        // si había un listener viejo → lo saco
        likeListeners[comentarioId]?.remove()

        val listener: ListenerRegistration = repo.escucharLikes(comentarioId) { listaLikes ->

            likesMap[comentarioId] = listaLikes.size
            _likesLive.postValue(likesMap.toMap())
        }

        likeListeners[comentarioId] = listener
    }


    // --------------------------------------------------------------
    // MANEJO DE ERRORES
    // --------------------------------------------------------------
    private fun manejarFirebaseError(e: Exception, userIdEnviado: String? = "") {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: "No autenticado"

        val msg = """
🔥 Firebase bloqueó la operación
${e.message}

⚠ Firestore exige:
request.resource.data.userId == request.auth.uid

• userId enviado: $userIdEnviado
• auth.uid real: $authUid
""".trimIndent()

        _error.postValue(msg)
    }


    // --------------------------------------------------------------
    // CREAR COMENTARIO
    // --------------------------------------------------------------
    fun enviarMensaje(
        texto: String,
        userName: String,
        fotoUrl: String = ""
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            return
        }

        if (texto.length > 250) {
            _error.value = "Máximo 250 caracteres."
            return
        }

        viewModelScope.launch {
            try {
                val comentario = Comentario(
                    id = "",
                    userId = uid,
                    userName = userName,
                    mensaje = texto,
                    fotoUrl = fotoUrl,
                    timestamp = System.currentTimeMillis()
                )

                val r = repo.enviarComentario(comentario)
                if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception)

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }


    // --------------------------------------------------------------
    // EDITAR
    // --------------------------------------------------------------
    fun editarComentario(comentarioId: String, nuevoMensaje: String, isAdmin: Boolean = false) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            try {
                val r = repo.editarComentario(comentarioId, nuevoMensaje, uid, isAdmin)
                if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception, uid)

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }


    // --------------------------------------------------------------
    // ELIMINAR
    // --------------------------------------------------------------
    fun eliminarComentario(comentarioId: String, isAdmin: Boolean = false) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            try {
                val r = repo.eliminarComentario(comentarioId, uid, isAdmin)
                if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception, uid)

            } catch (e: Exception) {
                manejarFirebaseError(e)
            }
        }
    }


    // --------------------------------------------------------------
    // LIKE / UNLIKE
    // --------------------------------------------------------------
    fun toggleLike(comentarioId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            _error.value = "Usuario no autenticado"
            return
        }

        viewModelScope.launch {
            try {
                val likes = repo.obtenerLikes(comentarioId)
                val tieneLike = likes.any { it.userId == uid }

                if (!tieneLike) {
                    val r = repo.agregarLike(comentarioId, uid)
                    if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception, uid)
                } else {
                    val r = repo.quitarLike(comentarioId, uid)
                    if (r.isFailure) manejarFirebaseError(r.exceptionOrNull()!! as Exception, uid)
                }

            } catch (e: Exception) {
                manejarFirebaseError(e, uid)
            }
        }
    }


    // EXPONER REPO (si lo necesitas)
    val repoPublic get() = repo


    // --------------------------------------------------------------
    // LIMPIEZA
    // --------------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        likeListeners.values.forEach { it.remove() }
        likeListeners.clear()
    }
}
