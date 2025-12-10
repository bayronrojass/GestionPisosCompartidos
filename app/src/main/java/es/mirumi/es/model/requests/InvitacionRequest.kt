package es.mirumi.es.model.requests

data class InvitacionRequest(
    val casaId: Long,
    val emailDestinatario: String,
    val remitenteId: Long,
)
