package com.example.gestionpisoscompartidos.model

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id")
    val id: Long,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("correo")
    val correo: String,
)
