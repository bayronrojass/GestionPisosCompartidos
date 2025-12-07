package com.example.gestionpisoscompartidos.model.dtos

open class PostItDTO(
    val id: Long? = null,
    val lienzoId: Long? = null,
    val posicionX: Float = 0f,
    val posicionY: Float = 0f,
    val width: Int = 0,
    val height: Int = 0,
    val localizacion: String,
)
