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
            // 1. Autenticar al usuario con Firebase Authentication
            val authResult = auth.signInWithEmailAndPassword(correo, clave).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                fetchUserDataFromFirestore(firebaseUser.uid, correo)
            } else {
                null
            }
        } catch (e: Exception) {
            // Captura errores de autenticación
            null
        }
    }

    private suspend fun fetchUserDataFromFirestore(uid: String, correo: String): Usuario? {
        return try {
            // Caso especial para el admin
            if (correo == "admin@vidasalud.cl") {
                return Usuario(
                    correo = correo,
                    nombre = "Administrador",
                    rol = "admin"
                )
            }

            // Obtener el documento directamente por UID
            val docSnapshot = db.collection("usuario")
                .document(uid)
                .get()
                .await()

            if (docSnapshot.exists()) {
                Usuario(
                    correo = docSnapshot.getString("correo") ?: correo,
                    nombre = docSnapshot.getString("nombre") ?: "Cliente",
                    rol = docSnapshot.getString("rol") ?: "cliente"
                )
            } else {
                // Usuario registrado en Auth pero sin datos en Firestore
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
