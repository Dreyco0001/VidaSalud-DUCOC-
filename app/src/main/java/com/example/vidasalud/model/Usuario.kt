package com.example.vidasalud.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = "",
    val rol: String = "",
    val clave: String? = null,
    val fechaRegistro: String? = null,
    val fotoUrl: String? = null,
    val estatura: Float? = null,
    val sexo: String? = null,

    // Propiedades de peso
    val pesoActual: Float? = null,
    val fechaPesoActual: String? = null,
    val pesoAnterior: Float? = null,
    val fechaPesoAnterior: String? = null,
    val pesoAnteanterior: Float? = null,
    val fechaPesoAnteanterior: String? = null
)
