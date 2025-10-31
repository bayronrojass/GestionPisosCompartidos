package com.example.gestionpisoscompartidos.model.dtos

data class PointDeltaDTO(
    // x e y estaban en Short, pero en pizarraView meten float como parametros
    val x: Float,
    val y: Float,
    val size: Float,
    var color: Byte,
)
