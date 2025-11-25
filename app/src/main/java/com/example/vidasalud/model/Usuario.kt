package com.example.vidasalud.model

data class Usuario(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = "",
    val clave: String = "",
    val rol: String = "",
    val fotoUrl: String? = null,
    val fotoPerfil: String? = null,
    val usarFotoRemota: Boolean = false
)
