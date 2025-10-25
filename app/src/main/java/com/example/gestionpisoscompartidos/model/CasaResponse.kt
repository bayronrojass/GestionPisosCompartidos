package com.example.gestionpisoscompartidos.model

data class CasaResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fechaCreacion: String,
)
