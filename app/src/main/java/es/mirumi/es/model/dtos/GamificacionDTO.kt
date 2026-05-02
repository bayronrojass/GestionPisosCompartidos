package es.mirumi.es.model.dtos

data class PerfilGamificacionDTO(
    val usuarioId: Long,
    val puntosConvivencia: Int,
    val logros: List<UsuarioLogroDTO>,
)

data class UsuarioLogroDTO(
    val logroId: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val nivel: String,
    val meta: Int,
    val progresoActual: Int,
    val estaCompletado: Boolean,
    val fechaCompletado: String?,
)
