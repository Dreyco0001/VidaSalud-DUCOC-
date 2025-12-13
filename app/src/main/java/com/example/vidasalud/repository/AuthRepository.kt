package com.example.vidasalud.repository

import com.example.vidasalud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Usuario? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(correo, clave).await()
            val uid = authResult.user?.uid ?: throw Exception("Correo o contraseña incorrectos")

            val doc = db.collection("usuario").document(uid).get().await()

            if (!doc.exists()) {
                throw Exception("Correo o contraseña incorrectos")
            }

            doc.toObject(Usuario::class.java)
        } catch (e: Exception) {
            throw Exception("Correo o contraseña incorrectos")
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
                    uid = doc.id, // Usar el ID del documento como UID
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

    suspend fun register(correo: String, clave: String, nombre: String): Usuario? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
            authResult.user?.let { firebaseUser ->
                val newUser = Usuario(
                    uid = firebaseUser.uid,
                    correo = correo,
                    nombre = nombre,
                    rol = "cliente"
                )
                db.collection("usuario").document(firebaseUser.uid).set(newUser).await()
                newUser
            }
        } catch (e: Exception) {
            null
        }
    }
}
