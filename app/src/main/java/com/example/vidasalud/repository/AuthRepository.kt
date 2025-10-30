package com.example.vidasalud.repository

import com.example.vidasalud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Usuario? {
        if (correo == "admin@vidasalud.cl") {
            return try {
                auth.signInWithEmailAndPassword(correo, clave).await()
                Usuario(correo = correo, nombre = "Administrador", rol = "admin")
            } catch (e: Exception) {
                null
            }
        } else {
            return loginWithFirestore(correo, clave)
        }
    }

    private suspend fun loginWithFirestore(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()

            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Usuario(
                    correo = doc.getString("correo") ?: "",
                    nombre = doc.getString("nombre") ?: "Usuario",
                    rol = doc.getString("rol") ?: "cliente"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
