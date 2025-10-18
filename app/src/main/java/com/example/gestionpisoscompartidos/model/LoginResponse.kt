package com.example.gestionpisoscompartidos.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para recibir la respuesta del endpoint /login.
 */
data class LoginResponse(
    // Token de autenticación
    @SerializedName("token")
    val authToken: String,
    // Lista de pisos asociados al usuario
    @SerializedName("pisos")
    val flats: List<Piso>,
    // Objeto Usuario
    @SerializedName("usuario")
    val user: Usuario,
)
