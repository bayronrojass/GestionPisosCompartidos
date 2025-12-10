package es.mirumi.es.model.responses

data class CasaResponse(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fechaCreacion: String,
)
