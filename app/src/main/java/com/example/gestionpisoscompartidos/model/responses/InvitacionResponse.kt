package com.example.gestionpisoscompartidos.model.responses

data class InvitacionResponse(
    val id: Long,
    val casaNombre: String,
    val remitenteNombre: String,
    val destinatarioEmail: String,
)
