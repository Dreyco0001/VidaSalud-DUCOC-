package com.example.vidasalud.model

data class Like(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)