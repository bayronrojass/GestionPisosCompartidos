package com.example.gestionpisoscompartidos.model

enum class CategoriaGasto { ALQUILER, COMIDA, SUMINISTROS, OCIO, OTROS }

data class Gasto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val importe: Double,
    val fecha: String,
    val categoria: CategoriaGasto,
    val pagadoPor: Usuario?
)