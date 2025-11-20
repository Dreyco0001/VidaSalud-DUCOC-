package com.example.vidasalud.model

data class Usuario(
    val uid: String = "",
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "",
    val fotoUrl: String? = null
)
