package es.mirumi.es.model.requests

data class TareaRequest(
    val nombre: String?,
    val descripcion: String?,
    val completado: Boolean?,
    val fechaFin: String?,
    val frecuencia: String?,
    val periodica: Boolean?,
    val asignadoAId: Long?,
    val prioridad: String?,
)
