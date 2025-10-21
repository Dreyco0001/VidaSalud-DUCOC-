package com.example.vidasalud.repository

import com.example.vidasalud.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoProductos(
    val productos: List<Producto>,
    val ultimoDocumento: Any?
)

class ProductoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerProductos(limite: Int = 10): ResultadoProductos {
        return try {
            val query = db.collection("producto")
                .whereGreaterThan("stock", 0)
                .orderBy("stock", Query.Direction.DESCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val productos = querySnapshot.documents.map { document ->
                Producto(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    precio = document.getDouble("precio") ?: 0.0,
                    imagen = document.getString("imagen") ?: "",
                    stock = document.getLong("stock")?.toInt() ?: 0
                )
            }

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoProductos(productos, ultimoDocumento)
        } catch (e: Exception) {
            ResultadoProductos(emptyList(), null)
        }
    }

    suspend fun obtenerMasProductos(limite: Int = 10, ultimoDocumento: Any?): ResultadoProductos {
        return try {
            if (ultimoDocumento == null) return ResultadoProductos(emptyList(), null)

            val query = db.collection("producto")
                .whereGreaterThan("stock", 0)
                .orderBy("stock", Query.Direction.DESCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val productos = querySnapshot.documents.map { document ->
                Producto(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    precio = document.getDouble("precio") ?: 0.0,
                    imagen = document.getString("imagen") ?: "",
                    stock = document.getLong("stock")?.toInt() ?: 0
                )
            }

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoProductos(productos, nuevoUltimoDocumento)
        } catch (e: Exception) {
            ResultadoProductos(emptyList(), null)
        }
    }

    // Actualizar stock en Firestore
    suspend fun actualizarStock(productoId: String, nuevoStock: Int): Boolean {
        return try {
            db.collection("producto")
                .document(productoId)
                .update("stock", nuevoStock)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Obtener producto por ID
    suspend fun obtenerProductoPorId(productoId: String): Producto? {
        return try {
            val document = db.collection("producto").document(productoId).get().await()
            if (document.exists()) {
                Producto(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    precio = document.getDouble("precio") ?: 0.0,
                    imagen = document.getString("imagen") ?: "",
                    stock = document.getLong("stock")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}