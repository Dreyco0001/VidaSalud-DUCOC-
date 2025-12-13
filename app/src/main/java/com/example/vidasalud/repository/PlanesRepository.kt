package com.example.vidasalud.repository

import com.example.vidasalud.model.Plan
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlanesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val planesCollection = db.collection("planes")

    // ---------------------------------------------------
    //  CREAR PLAN
    // ---------------------------------------------------
    suspend fun crearPlan(
        nombre: String,
        duracion: Int,
        nivel: String,
        objetivo: String,
        imagenUrl: String?,
        precio: Int,
        comidaRecomendada: String,
        imagenComidaUrl: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "nombre" to nombre,
                "duracion" to duracion,
                "nivel" to nivel,
                "objetivo" to objetivo,
                "imagenUrl" to (imagenUrl ?: ""),
                "precio" to precio,
                // 🔥 nombres alineados con el MODEL
                "comidaRecomendada" to comidaRecomendada,
                "imagenComidaUrl" to imagenComidaUrl
            )

            planesCollection.add(data).await()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    // ---------------------------------------------------
    //  OBTENER PLANES UNA SOLA VEZ
    // ---------------------------------------------------
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

    // ---------------------------------------------------
    //  ESCUCHA EN TIEMPO REAL
    // ---------------------------------------------------
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

    // ---------------------------------------------------
    //  ACTUALIZAR PLAN
    // ---------------------------------------------------
    suspend fun actualizarPlan(
        id: String,
        nombre: String? = null,
        duracion: Int? = null,
        nivel: String? = null,
        objetivo: String? = null,
        imagenUrl: String? = null,
        precio: Int? = null,
        comidaRecomendada: String? = null,
        imagenComidaUrl: String? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>()

            nombre?.let { updates["nombre"] = it }
            duracion?.let { updates["duracion"] = it }
            nivel?.let { updates["nivel"] = it }
            objetivo?.let { updates["objetivo"] = it }
            imagenUrl?.let { updates["imagenUrl"] = it }
            precio?.let { updates["precio"] = it }
            comidaRecomendada?.let { updates["comidaRecomendada"] = it }
            imagenComidaUrl?.let { updates["imagenComidaUrl"] = it }

            if (updates.isNotEmpty()) {
                planesCollection.document(id).update(updates).await()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---------------------------------------------------
    //  ELIMINAR PLAN
    // ---------------------------------------------------
    suspend fun eliminarPlan(id: String): Result<Unit> {
        return try {
            planesCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
