package com.example.gestionpisoscompartidos.model.dtos

data class PostItDTO(
    val id: Long,
    val lienzoId: Long,
    val posicionX: Float,
    val posicionY: Float,
    val plegado: Boolean,
)
