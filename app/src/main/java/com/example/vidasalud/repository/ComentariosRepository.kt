package com.example.vidasalud.repository

import com.example.vidasalud.model.Comentario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ComentariosRepository {

    private val db = FirebaseFirestore.getInstance()

    // ──────────────────────────────────────────────
    // CREAR COMENTARIO
    // ──────────────────────────────────────────────
    suspend fun agregarComentario(
        planId: String,
        userId: String,
        userName: String,
        mensaje: String
    ): Result<Unit> {

        return try {

            val comentarioData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "mensaje" to mensaje,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("planes")
                .document(planId)
                .collection("comentarios")
                .add(comentarioData)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────
    // OBTENER COMENTARIOS (una sola vez)
    // ──────────────────────────────────────────────
    suspend fun obtenerComentarios(planId: String): List<Comentario> {
        return try {
            val snapshot = db.collection("planes")
                .document(planId)
                .collection("comentarios")
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comentario::class.java)?.copy(id = doc.id)
            }

        } catch (_: Exception) {
            emptyList()
        }
    }

    // ──────────────────────────────────────────────
    // ESCUCHAR CAMBIOS REALTIME
    // ──────────────────────────────────────────────
    fun escucharComentarios(planId: String, onChange: (List<Comentario>) -> Unit) {

        db.collection("planes")
            .document(planId)
            .collection("comentarios")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }

                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comentario::class.java)?.copy(id = doc.id)
                }

                onChange(lista)
            }
    }

    // ──────────────────────────────────────────────
    // ELIMINAR COMENTARIO
    // ──────────────────────────────────────────────
    suspend fun eliminarComentario(planId: String, comentarioId: String): Result<Unit> {
        return try {
            db.collection("planes")
                .document(planId)
                .collection("comentarios")
                .document(comentarioId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}