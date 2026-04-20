package es.mirumi.es.model.requests

data class AportacionRequest(
    val usuarioId: Long,
    val cantidad: Double,
)

data class GastoRequest(
    val nombre: String,
    val importe: Double,
    val categoria: String,
    val pagadoPorId: Long? = null,
    val aportaciones: List<AportacionRequest> = emptyList(),
    val beneficiarios: List<String> = emptyList(),
)
