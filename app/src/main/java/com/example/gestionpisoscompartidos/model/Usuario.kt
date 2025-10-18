package com.example.gestionpisoscompartidos.model

import com.google.gson.annotations.SerializedName

data class Usuario(
// El ID de la base de datos, mapeado al campo "id" en el JSON
    @SerializedName("id")
    val id: Long,
    // El nombre, mapeado al campo "nombre" en el JSON
    @SerializedName("nombre")
    val nombre: String,
    // El correo, mapeado al campo "correo" en el JSON
    @SerializedName("correo")
    val correo: String,
)
