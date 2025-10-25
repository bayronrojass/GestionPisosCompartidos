package com.example.gestionpisoscompartidos.ui.piso.gestionUsuarios

data class MiembroPiso(
    val id: Long,
    val nombre: String,
    val esAdmin: Boolean,
    val esTu: Boolean,
    val colorIndicator: Int, // ej. R.color.holo_red_light
)
