package es.mirumi.es.model.requests

data class ElementoRequest(
    val nombre: String,
    val descripcion: String?,
    val completado: Boolean,
)
