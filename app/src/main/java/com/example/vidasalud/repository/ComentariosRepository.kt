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
        mensaje: String,
        timestamp: Long // <-- Added timestamp
    ): Result<Unit> {

        return try {

            val comentarioData = hashMapOf(
                "userId" to userId,
                "userName" to userName,
                "mensaje" to mensaje,
                "timestamp" to timestamp // <-- Use passed timestamp
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

    // ──────────────────────────────────────────────
    // AGREGAR COMENTARIOS DE EJEMPLO
    // ──────────────────────────────────────────────
    suspend fun agregarComentariosDeEjemplo(planId: String) {
        val comentarios = listOf(
            Comentario(userId = "user1", userName = "deportista_entusiasta", mensaje = "¡Me encantan estos ejercicios! He notado una gran mejora en mi resistencia."),
            Comentario(userId = "user2", userName = "fitness_fan", mensaje = "Excelentes rutinas, muy bien explicadas y efectivas."),
            Comentario(userId = "user3", userName = "atleta_comprometido", mensaje = "He probado muchos planes, y este es el mejor con diferencia. ¡Resultados visibles en poco tiempo!"),
            Comentario(userId = "user4", userName = "vida_saludable", mensaje = "Una comunidad muy motivadora y ejercicios que realmente funcionan. ¡Gracias!"),
            Comentario(userId = "user5", userName = "crossfit_lover", mensaje = "Los ejercicios son desafiantes pero gratificantes. ¡Totalmente recomendado!")
        )

        comentarios.forEach { comentario ->
            agregarComentario(planId, comentario.userId, comentario.userName, comentario.mensaje, comentario.timestamp)
        }
    }
}