package com.example.vidasalud.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun registrarUsuario(correo: String, clave: String, nombre: String): Result<Unit> {
        return try {
            // 1. Crear el usuario en Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 2. Crear un objeto con los datos para Firestore
                val nuevoUsuario = hashMapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "clave" to clave,
                    "rol" to "cliente" // ¡IMPORTANTE! Asignar un rol por defecto
                )

                // 3. Guardar el objeto en la colección "users" de Firestore
                // Usamos el UID del usuario de Auth como ID del documento
                db.collection("usuario").document(firebaseUser.uid)
                    .set(nuevoUsuario)
                    .await() // Espera a que la operación de guardado termine

                Result.success(Unit) // Devuelve éxito si todo fue bien
            } else {
                // Esto es raro, pero es bueno manejarlo
                Result.failure(Exception("No se pudo obtener el usuario de Firebase después de la creación."))
            }
        } catch (e: Exception) {
            // Captura errores de Auth (ej: email ya existe) o de Firestore
            Result.failure(e)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}