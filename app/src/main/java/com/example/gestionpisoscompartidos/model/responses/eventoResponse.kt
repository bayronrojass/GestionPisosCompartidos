package com.example.gestionpisoscompartidos.model.responses

data class eventoResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fechaInicio: String,
    val fechaFin: String?,
    val creadoPor: Long,
    val asistentes: MutableList<Long>,
)
