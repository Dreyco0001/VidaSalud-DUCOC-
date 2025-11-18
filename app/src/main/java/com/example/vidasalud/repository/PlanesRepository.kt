package com.example.vidasalud.repository

import com.example.vidasalud.model.Plan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlanesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val planesCollection = db.collection("planes")

    // CREAR PLAN (con URL directa)
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

    // OBTENER PLANES
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

    // ESCUCHAR CAMBIOS REALTIME
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
