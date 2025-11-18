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

    // REGISTRO (ORIGINAL)
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
                    "fotoUrl" to null
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

    // SUBIR FOTO PERFIL (ORIGINAL)
    suspend fun actualizarFotoPerfil(uriBytes: ByteArray): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            val ref = storage.reference.child("perfil/$uid.jpg")

            ref.putBytes(uriBytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()

            db.collection("usuario").document(uid)
                .update("fotoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CREAR USUARIO (NUEVO)
    suspend fun crearUsuario(
        correo: String,
        clave: String,
        nombre: String,
        rol: String = "cliente",
        fotoUrl: String? = null
    ): Result<Unit> {
        return try {
            val existing = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (existing.documents.isNotEmpty()) {
                return Result.failure(Exception("Ya existe un usuario con ese correo"))
            }

            val nuevo = hashMapOf(
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to rol,
                "fotoUrl" to fotoUrl
            )

            db.collection("usuario").add(nuevo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ACTUALIZAR USUARIO POR CORREO (NUEVO)
    suspend fun actualizarUsuarioPorCorreo(
        correo: String,
        nombre: String? = null,
        clave: String? = null,
        rol: String? = null,
        fotoUrl: String? = null
    ): Result<Unit> {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (query.documents.isEmpty()) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val docId = query.documents.first().id
            val updates = mutableMapOf<String, Any?>()

            nombre?.let { updates["nombre"] = it }
            clave?.let { updates["clave"] = it }
            rol?.let { updates["rol"] = it }
            fotoUrl?.let { updates["fotoUrl"] = it }

            if (updates.isNotEmpty()) {
                db.collection("usuario")
                    .document(docId)
                    .update(updates)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // OBTENER USUARIOS (NUEVO)
    suspend fun obtenerUsuarios(): Result<List<Map<String, Any>>> {
        return try {
            val query = db.collection("usuario").get().await()
            Result.success(query.documents.map { it.data ?: emptyMap() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ACTUALIZAR ROL (NUEVO)
    suspend fun actualizarRol(uid: String, nuevoRol: String): Result<Unit> {
        return try {
            db.collection("usuario").document(uid)
                .update("rol", nuevoRol)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ELIMINAR USUARIO (NUEVO)
    suspend fun eliminarUsuario(uid: String): Result<Unit> {
        return try {
            db.collection("usuario").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FECHA (ORIGINAL)
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
