package com.example.gestionpisoscompartidos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Lista(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
) : Parcelable
