package com.example.gestionpisoscompartidos.model.dtos

class ImagenDTO(
    id: Long,
    lienzoId: Long,
    posicionX: Float,
    posicionY: Float,
    width: Int,
    height: Int,
    plegado: Boolean,
) : PostItDTO(id, lienzoId, posicionX, posicionY, width, height, plegado)
