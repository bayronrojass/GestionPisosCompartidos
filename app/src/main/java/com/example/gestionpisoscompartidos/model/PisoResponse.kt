package com.example.gestionpisoscompartidos.model

data class PisoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val direccion: String?,
    val fechaCreacion: String,
)
