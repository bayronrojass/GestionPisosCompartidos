package es.mirumi.es.model.responses

import es.mirumi.es.model.Usuario
import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para recibir la respuesta del endpoint /login.
 */
data class LoginResponse(
    // Token de autenticación
    @SerializedName("authToken")
    val authToken: String,
    // Lista de pisos asociados al usuario
    @SerializedName("flats")
    val flats: List<CasaResponse>,
    // Objeto Usuario
    @SerializedName("user")
    val user: Usuario,
)
