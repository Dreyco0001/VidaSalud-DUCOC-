package com.example.vidasalud.model

data class Plan(
    val id: String = "",
    val nombre: String = "",
    val duracion: Int = 0,
    val nivel: String = "",
    val objetivo: String = "",
    val imagenUrl: String = "",
    val likesCount: Int = 0,
    val comentariosCount: Int = 0
)
