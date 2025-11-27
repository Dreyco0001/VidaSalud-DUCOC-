package com.example.vidasalud.model

data class Comentario(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val mensaje: String = "",
    val fotoUrl: String? = "",
    val timestamp: Long = System.currentTimeMillis()
)