package com.example.gestionpisoscompartidos.model.responses

import com.example.gestionpisoscompartidos.model.Usuario

data class CasaDetailsResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val miembros: List<Usuario>,
    val administradores: List<Usuario>,
)
