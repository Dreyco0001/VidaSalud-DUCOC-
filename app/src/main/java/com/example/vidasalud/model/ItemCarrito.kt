package com.example.vidasalud.model

data class ItemCarrito(
    val producto: Producto,
    var cantidad: Int = 1
)