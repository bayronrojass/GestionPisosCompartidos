package com.example.gestionpisoscompartidos.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    // Asume que tu backend espera "correo" y "contrasena"
    @SerializedName("correo")
    val email: String,
    @SerializedName("contrasena")
    val password: String,
)
