package es.mirumi.es.model.requests

import com.google.gson.annotations.SerializedName

data class RegistroRequest(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("contrasena")
    val contrasena: String,
    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,
)
