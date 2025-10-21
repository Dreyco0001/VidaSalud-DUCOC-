package com.example.vidasalud.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val imagen: String = "",
    val stock: Int = 0
)