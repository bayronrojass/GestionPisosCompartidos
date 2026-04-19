package es.mirumi.es.model.dtos

import com.google.gson.annotations.SerializedName

data class BorradorGastoDTO(
    @SerializedName("concepto")
    val concepto: String,
    @SerializedName("total")
    val total: Double,
    @SerializedName("urlTicket")
    val urlTicket: String,
)
