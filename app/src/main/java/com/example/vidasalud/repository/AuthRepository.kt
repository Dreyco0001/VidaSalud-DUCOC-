package com.example.vidasalud.repository

import  com.example.vidasalud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Usuario? {
        return try {
            // 1. Autenticar a TODOS los usuarios con Firebase Authentication.
            val authResult = auth.signInWithEmailAndPassword(correo, clave).await()

            // 2. Si la autenticación es exitosa, buscar los datos del usuario en Firestore.
            if (authResult.user != null) {
                fetchUserDataFromFirestore(correo)
            } else {
                null
            }
        } catch (e: Exception) {
            // Esto capturará intentos de login fallidos (contraseña incorrecta, usuario no encontrado, etc.)
            null
        }
    }

    private suspend fun fetchUserDataFromFirestore(correo: String): Usuario? {
        return try {
            // Caso especial para el admin.
            if (correo == "admin@vidasalud.cl") {
                return Usuario(
                    correo = correo,
                    nombre = "Administrador",
                    rol = "admin"
                )
            }

            // Para los demás usuarios, obtener sus datos de la colección "usuario".
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val doc = query.documents[0]
                Usuario(
                    correo = doc.getString("correo") ?: "",
                    // Ya no necesitamos la clave aquí. Asumimos que el constructor de Usuario
                    // tiene un valor por defecto o permite construirlo sin la clave.
                    nombre = doc.getString("nombre") ?: "Cliente",
                    rol = doc.getString("rol") ?: "cliente"
                )
            } else {
                // El usuario existe en Firebase Auth, pero no tiene un registro en la colección "usuario".
                // Devolvemos null para indicar un perfil incompleto.
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
