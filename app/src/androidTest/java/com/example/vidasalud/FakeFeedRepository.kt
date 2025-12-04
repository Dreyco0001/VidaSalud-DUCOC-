package com.example.vidasalud

import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.example.vidasalud.repository.FeedRepository
import com.google.firebase.firestore.ListenerRegistration

class FakeFeedRepository : FeedRepository() {

    val comentariosSimulados = mutableListOf<Comentario>()
    val likesSimulados = mutableMapOf<String, MutableList<Like>>()
    var enviarComentarioFallado = false

    override suspend fun enviarComentario(comentario: Comentario): Result<Unit> {
        return if (enviarComentarioFallado) {
            Result.failure(Exception("Error simulado"))
        } else {
            val comentarioConId = comentario.copy(id = comentario.id.ifEmpty { "fake_${comentariosSimulados.size}" })
            comentariosSimulados.add(comentarioConId)
            Result.success(Unit)
        }
    }

    override fun escucharComentarios(onChange: (List<Comentario>) -> Unit) {
        onChange(comentariosSimulados.toList())
    }

    override fun escucharLikes(comentarioId: String, onChange: (List<Like>) -> Unit): ListenerRegistration {
        val lista = likesSimulados.getOrPut(comentarioId) { mutableListOf() }
        onChange(lista.toList())
        return ListenerRegistration { }
    }

    override suspend fun agregarLike(comentarioId: String, userId: String): Result<Unit> {
        val lista = likesSimulados.getOrPut(comentarioId) { mutableListOf() }
        if (lista.any { it.userId == userId }) return Result.failure(Exception("Ya tiene like"))
        lista.add(Like(userId = userId, timestamp = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    override suspend fun quitarLike(comentarioId: String, userId: String): Result<Unit> {
        val lista = likesSimulados[comentarioId] ?: return Result.failure(Exception("No hay likes"))
        val removed = lista.removeIf { it.userId == userId }
        return if (removed) Result.success(Unit) else Result.failure(Exception("No existe like de ese usuario"))
    }

    override suspend fun obtenerLikes(comentarioId: String): List<Like> {
        return likesSimulados[comentarioId]?.toList() ?: emptyList()
    }
}
