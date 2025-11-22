package com.example.gestionpisoscompartidos.model

data class GastoRequest(
    val nombre: String,
    val descripcion: String = "",
    val importe: Double,
    val categoria: String,
    val pagadoPorId: Long,
)
