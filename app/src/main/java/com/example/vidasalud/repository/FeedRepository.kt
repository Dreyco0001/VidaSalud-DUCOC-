package com.example.vidasalud.repository

import com.example.vidasalud.model.Comentario
import com.example.vidasalud.model.Like
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FeedRepository {

    private val db = FirebaseFirestore.getInstance()
    private val comentariosRef = db.collection("feed_comentarios")
    private val usuariosRef = db.collection("usuario")

    // ------------------------------------------------------------------
    //              OBTENER FOTO DEL USUARIO
    // ------------------------------------------------------------------
    suspend fun obtenerFotoUsuario(userId: String): String? {
        return try {
            val doc = usuariosRef.document(userId).get().await()
            doc.getString("fotoUrl")
        } catch (e: Exception) {
            null // si falla devolvemos null → VM pone default_profile
        }
    }

    // ------------------------------------------------------------------
    //                       CREAR COMENTARIO
    // ------------------------------------------------------------------
    suspend fun enviarComentario(comentario: Comentario): Result<Unit> {
        return try {
            if (comentario.mensaje.length > 250) {
                return Result.failure(Exception("El comentario supera los 250 caracteres"))
            }

            val ref = comentariosRef.add(comentario).await()

            // 🔥 Actualizamos el ID del comentario (para edición/borrado)
            ref.update("id", ref.id).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------
    //                      OBTENER COMENTARIOS
    // ------------------------------------------------------------------
    suspend fun cargarComentarios(): List<Comentario> {
        return try {
            val snap = comentariosRef
                .orderBy("timestamp")
                .get()
                .await()

            snap.documents.mapNotNull { doc ->
                doc.toObject(Comentario::class.java)?.copy(
                    id = doc.id,
                    fotoUrl = doc.getString("fotoUrl") ?: "default_profile",
                    userName = doc.getString("userName") ?: "Usuario"
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
    // ------------------------------------------------------------------
//              OBTENER COMENTARIO POR ID (USADO EN VIEWMODEL)
// ------------------------------------------------------------------
    suspend fun obtenerComentarioPorId(id: String): Comentario? {
        return try {
            val doc = comentariosRef.document(id).get().await()
            if (!doc.exists()) return null

            // Mapear campos con fallback para evitar NPEs
            val mensaje = doc.getString("mensaje") ?: ""
            val userId = doc.getString("userId") ?: ""
            val userName = doc.getString("userName") ?: "Usuario"
            val fotoUrl = doc.getString("fotoUrl") ?: "default_profile"
            val timestamp = doc.getLong("timestamp") ?: 0L

            Comentario(
                id = doc.id,
                userId = userId,
                userName = userName,
                fotoUrl = fotoUrl,
                mensaje = mensaje,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            null
        }
    }


    // ------------------------------------------------------------------
    //                ESCUCHAR CAMBIOS EN TIEMPO REAL
    // ------------------------------------------------------------------
    fun escucharComentarios(onChange: (List<Comentario>) -> Unit) {
        comentariosRef
            .orderBy("timestamp")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snap.documents.mapNotNull { doc ->
                    doc.toObject(Comentario::class.java)?.copy(
                        id = doc.id,
                        fotoUrl = doc.getString("fotoUrl") ?: "default_profile",
                        userName = doc.getString("userName") ?: "Usuario"
                    )
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
            if (nuevoTexto.length > 250) {
                return Result.failure(Exception("El comentario supera los 250 caracteres"))
            }

            val doc = comentariosRef.document(comentarioId).get().await()
            if (!doc.exists()) return Result.failure(Exception("No existe"))

            val original = doc.toObject(Comentario::class.java)!!

            val diezMin = 10 * 60 * 1000
            val tiempoPasado = System.currentTimeMillis() - original.timestamp

            if (!isAdmin) {
                if (original.userId != userId)
                    return Result.failure(Exception("No puedes editar este comentario"))

                if (tiempoPasado > diezMin)
                    return Result.failure(Exception("Solo puedes editar durante los primeros 10 minutos"))
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

            if (!doc.exists()) return Result.failure(Exception("No existe"))

            val original = doc.toObject(Comentario::class.java)!!

            if (!isAdmin && original.userId != userId)
                return Result.failure(Exception("No puedes borrar este comentario"))

            comentariosRef.document(comentarioId).delete().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------------------------
    //                             LIKES
    // ------------------------------------------------------------------
    suspend fun agregarLike(comentarioId: String, userId: String): Result<Unit> {
        return try {
            val likeData = mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            comentariosRef
                .document(comentarioId)
                .collection("likes")
                .add(likeData)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun quitarLike(comentarioId: String, likeId: String): Result<Unit> {
        return try {
            comentariosRef
                .document(comentarioId)
                .collection("likes")
                .document(likeId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerLikes(comentarioId: String): List<Like> {
        return try {
            val snap = comentariosRef
                .document(comentarioId)
                .collection("likes")
                .get()
                .await()

            snap.map { doc ->
                Like(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }

        } catch (_: Exception) {
            emptyList()
        }
    }
}
