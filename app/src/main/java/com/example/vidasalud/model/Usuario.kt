package com.example.vidasalud.model

data class Usuario (
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "" //Variable local para controlar los roles de los correos
)

