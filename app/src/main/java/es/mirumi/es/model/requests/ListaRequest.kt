package es.mirumi.es.model.requests

data class ListaRequest(
    val nombre: String,
    val descripcion: String?,
    val participantesIds: List<Long>? = null,
)
