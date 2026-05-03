package es.mirumi.es.model.responses

data class EncuestaResponse(
    val id: Long,
    val titulo: String,
    val creadorNombre: String,
    val estado: String, // "ABIERTA" o "CERRADA"
    val fechaCreacion: String,
    val opciones: List<OpcionResponse>,
    val haVotado: Boolean,
)
