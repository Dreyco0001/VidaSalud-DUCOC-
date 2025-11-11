package com.example.vidasalud.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UsuarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun registrarUsuario(correo: String, clave: String, nombre: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val nuevoUsuario = hashMapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "clave" to clave,
                    "rol" to "cliente",
                    "fechaRegistro" to getCurrentDate(),
                    "fotoUrl" to null // 🔹 Campo vacío por compatibilidad
                )

                db.collection("usuario").document(firebaseUser.uid)
                    .set(nuevoUsuario)
                    .await()

                Result.success(Unit)
            } else {
                Result.failure(Exception("No se pudo obtener el usuario de Firebase después de la creación."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Nueva función: sube imagen y guarda URL en Firestore
    suspend fun actualizarFotoPerfil(uriBytes: ByteArray): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val ref = storage.reference.child("perfil/$uid.jpg")

            // Subir imagen
            ref.putBytes(uriBytes).await()

            // Obtener URL de descarga
            val downloadUrl = ref.downloadUrl.await().toString()

            // Actualizar Firestore
            db.collection("usuario").document(uid)
                .update("fotoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
