package com.example.vidasalud.repository

import com.example.vidasalud.model.Plan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PlanesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val planesCollection = db.collection("planes")

    // CREAR PLAN
    suspend fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        imagenBytes: ByteArray? = null
    ): Result<Unit> {
        return try {
            var imageUrl = ""

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

    // OBTENER LISTA (UNA SOLA VEZ)
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

    // ESCUCHAR EN TIEMPO REAL
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

    // ACTUALIZAR PLAN
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
                val url = imagenRef.downloadUrl.await().toString()
                actualizaciones["imagenUrl"] = url
            }

            planesCollection.document(id).update(actualizaciones).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ELIMINAR PLAN
    suspend fun eliminarPlan(id: String): Result<Unit> {
        return try {
            planesCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
