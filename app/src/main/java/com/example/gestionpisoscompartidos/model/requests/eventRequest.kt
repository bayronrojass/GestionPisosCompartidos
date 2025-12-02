package com.example.gestionpisoscompartidos.model.requests

data class eventRequest(
    val nombre: String,
    val descripcion: String? = null,
    val fechaInicio: String,
    val fechaFin: String? = null,
    val creadoPor: Long,
)
