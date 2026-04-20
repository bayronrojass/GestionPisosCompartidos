package es.mirumi.es.model.requests

data class AportacionRequest(
    val usuarioId: Long,
    val cantidad: Double
)

data class GastoRequest(
    val nombre: String,
    val descripcion: String = "",
    val importe: Double,
    val categoria: String,
    val pagadoPorId: Long,
    val beneficiarios: List<String>,
)
