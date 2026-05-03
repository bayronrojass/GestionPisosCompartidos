package es.mirumi.es.model.requests

data class EncuestaRequest(
    val titulo: String,
    val opciones: List<String>,
)
