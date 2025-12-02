package com.example.vidasalud.model

data class Comentario(
    val id: String = "",              // ID del comentario (Firestore)
    val userId: String = "",          // ID del usuario que escribe
    val userName: String = "",        // Nombre del usuario
    val mensaje: String = "",         // Texto del comentario (NO "texto")
    val fotoUrl: String = "",         // Foto del usuario
    val timestamp: Long = System.currentTimeMillis() // Fecha/hora
)