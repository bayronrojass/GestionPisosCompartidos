package com.example.gestionpisoscompartidos.model

data class TareaRequest(
    val nombre: String?,
    val descripcion: String?,
    val completado: Boolean?,
    val fechaFin: String?,
    val frecuencia: String?,
    val periodica: Boolean?,
    val asignadoAId: Long?,
)
