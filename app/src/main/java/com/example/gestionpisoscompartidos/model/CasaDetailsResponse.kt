package com.example.gestionpisoscompartidos.model

data class CasaDetailsResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val miembros: List<Usuario>,
    val administradores: List<Usuario>,
)
