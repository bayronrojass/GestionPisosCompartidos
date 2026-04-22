package es.mirumi.es.model

import com.google.gson.annotations.SerializedName

data class AportacionDTO(
    val nombre: String,
    val cantidad: Double,
)

data class Gasto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null,
    @SerializedName("importe")
    val importe: Double = 0.0,
    val fecha: String = "",
    val categoria: String = "OTROS",
    @SerializedName("pagadoPorNombre")
    val pagadoPorNombre: String? = "Desconocido",
    @SerializedName("pagadoPorFotoUrl")
    val pagadoPorFotoUrl: String? = null,
    val aportaciones: List<AportacionDTO>? = emptyList(),
    val beneficiarios: List<String>? = emptyList(),
)
