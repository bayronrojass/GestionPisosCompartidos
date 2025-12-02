package com.example.gestionpisoscompartidos.model.dtos

open class PostItDTO(
    val id: Long,
    val lienzoId: Long,
    val posicionX: Float,
    val posicionY: Float,
    val width: Int,
    val height: Int,
    val localizacion: String,
)
