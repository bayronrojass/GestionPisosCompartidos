package es.mirumi.es.model.requests

data class PagoBizumRequest(
    val deudorId: Long,
    val acreedorId: Long,
    val cantidad: Double,
    val gastoId: Long? = null, // null = saldo total
    val estado: String = "PENDIENTE_CONFIRMACION",
)
