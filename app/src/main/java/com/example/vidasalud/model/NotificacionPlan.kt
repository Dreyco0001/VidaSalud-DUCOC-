package com.example.vidasalud.model

data class NotificacionPlan(
    val id: String = "",
    val planId: String = "",
    val nombrePlan: String = "",
    val descripcionPlan: String = "",
    val imagenPlanUrl: String = "",
    val precio: Int = 0,
    val comidaRecomendada: String = "",
    val imagenComidaUrl: String = "",
    val userId: String = "",
    val userRol: String = "cliente",
    val fechaToma: Long = System.currentTimeMillis(),
    val estado: String = "activo",
    val nombreUsuario: String = ""
)
