package es.mirumi.es.ui.piso.gestionUsuarios

data class MiembroPiso(
    val id: Long,
    val nombre: String,
    val esAdmin: Boolean,
    val esTu: Boolean,
    val colorIndicator: Int,
    val fotoUrl: String? = null,
)
