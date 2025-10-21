package com.example.vidasalud.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun registroUsuario(correo: String, clave: String, nombre: String): Boolean {
        return try {
            // Verificar si el correo ya existe
            val querySnapshot = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return false // El correo ya está registrado
            }

            // Crear nuevo usuario solo en Firestore
            val userData = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to "cliente",
                "fechaRegistro" to getCurrentDate()
            )

            // Agregar documento a la colección "usuario"
            db.collection("usuario").add(userData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}