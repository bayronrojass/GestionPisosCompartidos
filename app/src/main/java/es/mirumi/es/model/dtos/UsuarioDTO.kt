package es.mirumi.es.model.dtos

import com.google.gson.annotations.SerializedName

data class UsuarioDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("correo")
    val correo: String,
)
