package com.example.vidasalud.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID


// 🔹 Modelo de plan de ejercicio
data class PlanEjercicio(
    val id: String = "",
    val nombre: String = "",
    val duracion: Int = 0, // minutos
    val nivel: String = "",
    val objetivo: String = "",
    val imagenUrl: String = ""
)

class PlanesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val planesCollection = db.collection("planes")

    // 🔹 Crear plan (con o sin imagen)
    suspend fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        imagenBytes: ByteArray? = null
    ): Result<Unit> {
        return try {
            var imageUrl = ""

            // Si tiene imagen, subirla a Firebase Storage
            if (imagenBytes != null) {
                val imagenRef = storage.reference.child("planes/${UUID.randomUUID()}.jpg")
                imagenRef.putBytes(imagenBytes).await()
                imageUrl = imagenRef.downloadUrl.await().toString()
            }

            val nuevoPlan = hashMapOf(
                "nombre" to nombre,
                "duracion" to duracion,
                "nivel" to nivel,
                "objetivo" to objetivo,
                "imagenUrl" to imageUrl
            )

            planesCollection.add(nuevoPlan).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Leer todos los planes
    suspend fun obtenerPlanes(): List<PlanEjercicio> {
        return try {
            val snapshot = planesCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PlanEjercicio::class.java)?.copy(id = doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // 🔹 Actualizar plan
    suspend fun actualizarPlan(
        id: String,
        nombre: String? = null,
        duracion: Int? = null,
        nivel: String? = null,
        objetivo: String? = null,
        imagenBytes: ByteArray? = null
    ): Result<Unit> {
        return try {
            val actualizaciones = mutableMapOf<String, Any>()

            nombre?.let { actualizaciones["nombre"] = it }
            duracion?.let { actualizaciones["duracion"] = it }
            nivel?.let { actualizaciones["nivel"] = it }
            objetivo?.let { actualizaciones["objetivo"] = it }

            if (imagenBytes != null) {
                val imagenRef = storage.reference.child("planes/${UUID.randomUUID()}.jpg")
                imagenRef.putBytes(imagenBytes).await()
                val imageUrl = imagenRef.downloadUrl.await().toString()
                actualizaciones["imagenUrl"] = imageUrl
            }

            planesCollection.document(id).update(actualizaciones).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Eliminar plan
    suspend fun eliminarPlan(id: String): Result<Unit> {
        return try {
            planesCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
