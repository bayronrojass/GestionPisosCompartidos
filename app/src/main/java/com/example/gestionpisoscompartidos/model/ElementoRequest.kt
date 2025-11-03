package com.example.gestionpisoscompartidos.model

data class ElementoRequest(
    val nombre: String,
    val descripcion: String?,
    val completado: Boolean,
)
