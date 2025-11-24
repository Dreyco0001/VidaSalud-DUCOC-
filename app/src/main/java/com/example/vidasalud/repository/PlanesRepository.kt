package com.example.vidasalud.repository

import com.example.vidasalud.model.Plan
import com.example.vidasalud.model.Like
import com.example.vidasalud.model.Comentario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlanesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val planesCollection = db.collection("planes")

    // --------------------------
    //   CRUD PRINCIPAL DE PLANES
    // --------------------------

    suspend fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        imagenUrl: String? = null
    ): Result<Unit> {
        return try {

            val nuevoPlan = hashMapOf(
                "nombre" to nombre,
                "duracion" to duracion,
                "nivel" to nivel,
                "objetivo" to objetivo,
                "imagenUrl" to (imagenUrl ?: "")
            )

            planesCollection.add(nuevoPlan).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerPlanes(): List<Plan> {
        return try {
            val snapshot = planesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Plan::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun escucharPlanes(onChange: (List<Plan>) -> Unit) {
        planesCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                onChange(emptyList())
                return@addSnapshotListener
            }

            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Plan::class.java)?.copy(id = doc.id)
            }

            onChange(lista)
        }
    }

    suspend fun actualizarPlan(
        id: String,
        nombre: String? = null,
        duracion: Int? = null,
        nivel: String? = null,
        objetivo: String? = null,
        imagenUrl: String? = null
    ): Result<Unit> {

        return try {
            val actualizaciones = mutableMapOf<String, Any>()

            nombre?.let { actualizaciones["nombre"] = it }
            duracion?.let { actualizaciones["duracion"] = it }
            nivel?.let { actualizaciones["nivel"] = it }
            objetivo?.let { actualizaciones["objetivo"] = it }
            imagenUrl?.let { actualizaciones["imagenUrl"] = it }

            planesCollection.document(id).update(actualizaciones).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarPlan(id: String): Result<Unit> {
        return try {
            planesCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------
    //       LIKES POR PLAN
    // --------------------------

    suspend fun agregarLike(planId: String, userId: String): Result<Unit> {
        return try {
            val likeData = mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            planesCollection.document(planId)
                .collection("likes")
                .add(likeData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun quitarLike(planId: String, likeId: String): Result<Unit> {
        return try {
            planesCollection.document(planId)
                .collection("likes")
                .document(likeId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerLikes(planId: String): List<Like> {
        return try {
            val snap = planesCollection.document(planId)
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

    // --------------------------
    //    COMENTARIOS POR PLAN
    // --------------------------

    suspend fun agregarComentario(planId: String, comentario: Comentario): Result<Unit> {
        return try {
            planesCollection.document(planId)
                .collection("comentarios")
                .add(comentario)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerComentarios(planId: String): List<Comentario> {
        return try {
            val snap = planesCollection.document(planId)
                .collection("comentarios")
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

    suspend fun eliminarComentario(planId: String, comentarioId: String): Result<Unit> {
        return try {
            planesCollection.document(planId)
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
