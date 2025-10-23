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
            //Intentar autenticar con auth
            when {
                correo == "admin@vidasalud.cl" -> {
                    //Autenticación con Firebase Auth
                    val resultado = auth.signInWithEmailAndPassword(correo, clave).await()
                    Usuario(
                        correo = correo,
                        nombre = "Administrador",
                        rol = "admin"
                    )
                }
                else -> {
                    //Autenticación con la colección usuario de Firestore
                    loginWithFirestore(correo,clave)
                }
            }
        } catch (e: Exception){
            null
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
                    clave = doc.getString("clave") ?: "",
                    nombre = doc.getString("nombre") ?: "Cliente",
                    rol = doc.getString("rol") ?: "cliente"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}