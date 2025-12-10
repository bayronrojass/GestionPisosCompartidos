package es.mirumi.es.model.requests

data class eventRequest(
    val nombre: String,
    val descripcion: String? = null,
    val fechaInicio: String,
    val fechaFin: String? = null,
    val creadoPor: Long,
)
