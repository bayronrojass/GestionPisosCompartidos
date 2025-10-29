package com.example.gestionpisoscompartidos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Elemento(
    val id: Long?,
    var nombre: String,
    var descripcion: String?,
    var completado: Boolean
) : Parcelable
