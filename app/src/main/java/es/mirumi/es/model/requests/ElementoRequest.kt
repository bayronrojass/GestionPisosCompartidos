package es.mirumi.es.model.requests

data class ElementoRequest(
    val nombre: String,
    val descripcion: String?,
    val completado: Boolean,
    val cantidad: Int = 1,
    val iconoKey: String? = null,
)
