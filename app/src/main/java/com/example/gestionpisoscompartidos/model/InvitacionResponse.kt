package com.example.gestionpisoscompartidos.model

data class InvitacionResponse(
    val id: Long,
    val casaNombre: String,
    val remitenteNombre: String,
    val destinatarioEmail: String,
)
