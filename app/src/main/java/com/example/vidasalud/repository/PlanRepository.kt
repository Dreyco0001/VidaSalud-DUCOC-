package com.example.vidasalud.repository


import com.example.vidasalud.model.Plan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PlanRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val planesRef = db.collection("planes")

    // 🔹 CREATE (agregar nuevo plan)
    suspend fun agregarPlan(plan: Plan, imagenBytes: ByteArray?): Result<Unit> {
        return try {
            var imageUrl = ""

            // Si hay imagen, la subimos a Firebase Storage
            if (imagenBytes != null) {
                val nombreArchivo = "planes/${UUID.randomUUID()}.jpg"
                val imagenRef = storage.reference.child(nombreArchivo)
                imagenRef.putBytes(imagenBytes).await()
                imageUrl = imagenRef.downloadUrl.await().toString()
            }

            // Creamos un nuevo documento en Firestore
            val docRef = planesRef.document()
            val nuevoPlan = plan.copy(id = docRef.id, imagenUrl = imageUrl)

            docRef.set(nuevoPlan).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 READ (obtener todos los planes)
    suspend fun obtenerPlanes(): List<Plan> {
        return try {
            val snapshot = planesRef.get().await()
            snapshot.documents.mapNotNull { it.toObject(Plan::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 UPDATE (modificar un plan)
    suspend fun actualizarPlan(plan: Plan, nuevaImagen: ByteArray?): Result<Unit> {
        return try {
            var imageUrl = plan.imagenUrl

            // Si hay nueva imagen, reemplazamos
            if (nuevaImagen != null) {
                val nombreArchivo = "planes/${UUID.randomUUID()}.jpg"
                val imagenRef = storage.reference.child(nombreArchivo)
                imagenRef.putBytes(nuevaImagen).await()
                imageUrl = imagenRef.downloadUrl.await().toString()
            }

            planesRef.document(plan.id).update(
                mapOf(
                    "nombre" to plan.nombre,
                    "duracion" to plan.duracion,
                    "nivel" to plan.nivel,
                    "objetivo" to plan.objetivo,
                    "imagenUrl" to imageUrl
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 DELETE (eliminar un plan)
    suspend fun eliminarPlan(id: String): Result<Unit> {
        return try {
            planesRef.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}