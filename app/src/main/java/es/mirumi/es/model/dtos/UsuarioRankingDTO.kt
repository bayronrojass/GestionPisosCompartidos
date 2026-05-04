package es.mirumi.es.model.dtos

data class UsuarioRankingDTO(
    val posicion: Int,
    val usuarioId: Long,
    val nombre: String,
    val fotoUrl: String?,
    val puntos: Int,
)
