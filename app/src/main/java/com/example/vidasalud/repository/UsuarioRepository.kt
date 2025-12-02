
package com.example.vidasalud.repository

import com.example.vidasalud.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --------------------------------------------------------------------
    // REGISTRO ORIGINAL (Auth + Firestore)
    // --------------------------------------------------------------------
    suspend fun registrarUsuario(correo: String, clave: String, nombre: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuario Firebase nulo."))

            val nuevoUsuario = hashMapOf(
                "uid" to firebaseUser.uid,
                "nombre" to nombre,
                "correo" to correo,
                "clave" to clave,
                "rol" to "cliente",
                "fechaRegistro" to getCurrentDate(),
                "fotoUrl" to null
            )

            db.collection("usuario")
                .document(firebaseUser.uid)
                .set(nuevoUsuario)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // SUBIR FOTO PERFIL
    // --------------------------------------------------------------------
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

    // --------------------------------------------------------------------
    // CREAR USUARIO (ADMIN)
    // --------------------------------------------------------------------
    suspend fun crearUsuario(
        correo: String,
        clave: String,
        nombre: String,
        rol: String = "cliente",
        fotoUrl: String? = null
    ): Result<Unit> {
        return try {
            // evitar duplicados
            val existing = db.collection("usuario")
                .whereEqualTo("correo", correo)
                .get()
                .await()

            if (existing.documents.isNotEmpty()) {
                return Result.failure(Exception("Ya existe un usuario con ese correo"))
            }

            // documento temporal sin UID
            val nuevo = hashMapOf(
                "uid" to "",
                "correo" to correo,
                "clave" to clave,
                "nombre" to nombre,
                "rol" to rol,
                "fotoUrl" to fotoUrl
            )

            val docRef = db.collection("usuario").add(nuevo).await()

            // actualizar UID en el documento real
            db.collection("usuario").document(docRef.id)
                .update("uid", docRef.id)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // ACTUALIZAR USUARIO POR CORREO
    // --------------------------------------------------------------------
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

    // --------------------------------------------------------------------
    // OBTENER USUARIOS (COMPATIBLE CON VIEWMODEL)
    // --------------------------------------------------------------------
    suspend fun obtenerUsuarios(): Result<List<Map<String, Any>>> {
        return try {
            val query = db.collection("usuario").get().await()

            // DEBUG: tamaño y primer doc
            android.util.Log.d("REPO_USUARIOS", "docs=${query.documents.size}")
            android.util.Log.d("REPO_USUARIOS", "primer=${query.documents.firstOrNull()?.data}")

            val lista = query.documents.map { doc ->
                val data = doc.data ?: emptyMap<String, Any>()
                data + ("uid" to doc.id)
            }

            Result.success(lista)

        } catch (e: Exception) {
            // LOG completo para saber por qué falla
            android.util.Log.e("REPO_USUARIOS", "Error obtenerUsuarios", e)
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // ACTUALIZAR SOLO ROL POR UID
    // --------------------------------------------------------------------
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

    suspend fun actualizarUsuarioPorUid(uid: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            db.collection("usuario")
                .document(uid)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // ELIMINAR USUARIO POR UID
    // --------------------------------------------------------------------
    suspend fun eliminarUsuario(uid: String): Result<Unit> {
        return try {
            db.collection("usuario")
                .document(uid)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // ACTUALIZAR PESO
    // --------------------------------------------------------------------
    suspend fun updateWeight(userId: String, newWeight: Float): Result<Unit> {
        return try {
            val userRef = db.collection("usuario").document(userId)
            val snapshot = userRef.get().await()
            val user = snapshot.toObject(Usuario::class.java)

            if (user != null) {
                val updates = hashMapOf(
                    "pesoAnteanterior" to user.pesoAnterior,
                    "pesoAnterior" to user.pesoActual,
                    "pesoActual" to newWeight,
                    "fechaPesoAnteanterior" to user.fechaPesoAnterior,
                    "fechaPesoAnterior" to user.fechaPesoActual,
                    "fechaPesoActual" to getCurrentDate()
                )
                userRef.update(updates as Map<String, Any?>).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --------------------------------------------------------------------
    // ACTUALIZAR ESTATURA
    // --------------------------------------------------------------------
    suspend fun updateHeight(userId: String, newHeight: Float): Result<Unit> {
        return try {
            val userRef = db.collection("usuario").document(userId)
            userRef.update("estatura", newHeight).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // --------------------------------------------------------------------
    // FECHA
    // --------------------------------------------------------------------
    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }
}
