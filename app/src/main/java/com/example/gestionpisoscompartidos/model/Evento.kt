package com.example.gestionpisoscompartidos.model

data class Evento(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fechaInicio: String,
    val fechaFin: String?,
    val creadoPorNombre: String
)