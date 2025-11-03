package com.example.gestionpisoscompartidos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Casa(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val rutaImagen: String?,
    val fechaCreacion: String,
) : Parcelable
