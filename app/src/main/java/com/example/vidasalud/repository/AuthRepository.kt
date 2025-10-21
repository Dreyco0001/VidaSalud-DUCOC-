package com.example.vidasalud.repositry

import com.example.vidasalud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(correo: String, clave: String): Usuario? {
        return try {
            //Intentar autenticar con Authentication de Firebase - admin
            val resultado = auth.signInWithEmailAndPassword(correo, clave).await()
            val usuario = resultado.user
            if (usuario != null ) {
                getUserFromFirestore(usuario.uid, correo) ?: Usuario (
                    correo = correo,
                    nombre = if (correo == "admin@nombrecaso.cl") "Administrador" else "Usuario",
                    rol = if (correo == "admin@nombrecaso.cl") "admin" else "cliente"
                )
            } else null
        }catch (e: Exception) {
            //Si falla Auth, intentar con Firestore
            loginWithFirestore(correo, clave)
        }
    }

    private suspend fun getUserFromFirestore(uid: String, correo: String): Usuario? {
        return try {
            val documento = db.collection("usuario").document(uid).get().await()
            if (documento.exists()) {
                Usuario(
                    correo = documento.getString("correo") ?: correo,
                    nombre = documento.getString("nombre") ?: "Usuario",
                    rol = documento.getString("rol") ?: "cliente"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun loginWithFirestore(correo: String, clave: String): Usuario? {
        return try {
            val query = db.collection("usuario")
                .whereEqualTo("correo",correo)
                .whereEqualTo("clave", clave)
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents[0]
                Usuario (
                    correo = doc.getString("correo") ?: "",
                    nombre = doc.getString("nombre") ?: "Cliente",
                    rol = doc.getString("rol") ?: "cliente"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
