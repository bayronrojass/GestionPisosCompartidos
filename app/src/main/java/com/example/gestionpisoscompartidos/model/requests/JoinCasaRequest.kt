package com.example.gestionpisoscompartidos.model.requests

/**
 * DTO (Data Transfer Object) que se envía al backend
 * en el cuerpo de la petición POST para unirse a una casa.
 */
data class JoinCasaRequest(
    val usuarioId: Long,
)
