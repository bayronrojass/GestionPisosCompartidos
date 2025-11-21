package com.example.gestionpisoscompartidos.model

import com.google.gson.annotations.SerializedName

data class Gasto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    // IMPORTANTE: Asegúrate de que este @SerializedName coincida con el backend
    @SerializedName("importe")
    val importe: Double = 0.0,
    val fecha: String = "",
    val categoria: String = "OTROS",
    // IMPORTANTE: El backend envía "pagadoPorNombre", aquí lo mapeamos
    @SerializedName("pagadoPorNombre")
    val pagadoPorNombre: String? = "Desconocido",
)
