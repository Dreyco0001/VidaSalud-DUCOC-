package com.example.vidasalud.repository

import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

open class FeedRepository {

    private val db = FirebaseFirestore.getInstance()
    private val comentariosRef = db.collection("feed_comentarios")

    // ------------------------------------------------------------------
    //                      CREAR COMENTARIO
    // ------------------------------------------------------------------
    open suspend fun enviarComentario(comentario: Comentario): Result<Unit> {
        return try {

            val nuevoId = comentariosRef.document().id
            val comentarioConId = comentario.copy(id = nuevoId)

            comentariosRef
                .document(nuevoId)
                .set(comentarioConId)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------
    //                      CARGAR COMENTARIOS
    // ------------------------------------------------------------------
    suspend fun cargarComentarios(): List<Comentario> {
        return try {
            val snap = comentariosRef
                .orderBy("timestamp")
                .get()
                .await()

            snap.documents.mapNotNull { doc ->
                doc.toObject(Comentario::class.java)?.copy(id = doc.id)
            }

        } catch (_: Exception) {
            emptyList()
        }
    }

    // ------------------------------------------------------------------
    //                OBTENER COMENTARIO POR ID
    // ------------------------------------------------------------------
    suspend fun obtenerComentarioPorId(id: String): Comentario? {
        return try {
            val doc = comentariosRef.document(id).get().await()
            if (!doc.exists()) return null

            doc.toObject(Comentario::class.java)?.copy(id = doc.id)

        } catch (_: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    //            ESCUCHAR CAMBIOS EN TIEMPO REAL
    // ------------------------------------------------------------------
    open fun escucharComentarios(onChange: (List<Comentario>) -> Unit) {
        comentariosRef
            .orderBy("timestamp")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snap.documents.mapNotNull { d ->
                    d.toObject(Comentario::class.java)?.copy(id = d.id)
                }

                onChange(lista)
            }
    }

    // ------------------------------------------------------------------
    //                           EDITAR
    // ------------------------------------------------------------------
    suspend fun editarComentario(
        comentarioId: String,
        nuevoTexto: String,
        userId: String,
        isAdmin: Boolean
    ): Result<Unit> {

        return try {

            if (nuevoTexto.length > 250)
                return Result.failure(Exception("Máximo 250 caracteres."))

            val doc = comentariosRef.document(comentarioId).get().await()
            if (!doc.exists())
                return Result.failure(Exception("No existe el comentario."))

            val original = doc.toObject(Comentario::class.java)!!

            val diezMin = 10 * 60 * 1000
            val tiempoPasado = System.currentTimeMillis() - original.timestamp

            // 🔥 ADMIN = libertad total
            if (!isAdmin) {
                if (original.userId != userId)
                    return Result.failure(Exception("No puedes editar este comentario."))

                if (tiempoPasado > diezMin)
                    return Result.failure(Exception("Solo puedes editar dentro de los 10 minutos."))
            }

            comentariosRef.document(comentarioId)
                .update("mensaje", nuevoTexto)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------
    //                      ELIMINAR COMENTARIO
    // ------------------------------------------------------------------
    suspend fun eliminarComentario(
        comentarioId: String,
        userId: String,
        isAdmin: Boolean
    ): Result<Unit> {

        return try {

            val doc = comentariosRef.document(comentarioId).get().await()
            if (!doc.exists())
                return Result.failure(Exception("No existe el comentario."))

            val original = doc.toObject(Comentario::class.java)!!

            // 🔥 ADMIN BORRA TODO LO QUE QUIERA
            if (!isAdmin && original.userId != userId)
                return Result.failure(Exception("No puedes borrar este comentario."))

            comentariosRef.document(comentarioId).delete().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------
    //      ESCUCHAR LIKES EN TIEMPO REAL (FUNCIONA PERFECTO)
    // ------------------------------------------------------------------
    open fun escucharLikes(
        comentarioId: String,
        onChange: (List<Like>) -> Unit
    ): ListenerRegistration {

        return comentariosRef
            .document(comentarioId)
            .collection("likes")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snap.documents.map { doc ->
                    Like(
                        userId = doc.getString("userId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }

                onChange(lista)
            }
    }



    // ------------------------------------------------------------------
    //                          LIKES
    // ------------------------------------------------------------------
    open suspend fun agregarLike(comentarioId: String, userId: String): Result<Unit> {
        return try {
            val likeRef = comentariosRef
                .document(comentarioId)
                .collection("likes")

            val snap = likeRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!snap.isEmpty)
                return Result.failure(Exception("Ya diste like."))

            val data = Like(
                userId = userId,
                timestamp = System.currentTimeMillis()
            )

            likeRef.add(data).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    open suspend fun quitarLike(comentarioId: String, userId: String): Result<Unit> {
        return try {
            val likeRef = comentariosRef
                .document(comentarioId)
                .collection("likes")

            val snap = likeRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (snap.isEmpty)
                return Result.failure(Exception("No tienes like en este comentario."))

            snap.documents.first().reference.delete().await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    open suspend fun obtenerLikes(comentarioId: String): List<Like> {
        return try {
            val snap = comentariosRef
                .document(comentarioId)
                .collection("likes")
                .get()
                .await()

            snap.documents.map { doc ->
                Like(
                    userId = doc.getString("userId") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }

        } catch (_: Exception) {
            emptyList()
        }
    }
}
