package com.example.gestionpisoscompartidos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tarea(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val completado: Boolean,
    val fechaFin: String?,
    val frecuencia: String?,
    val periodica: Boolean,
    val asignadoA: Usuario?,
    val prioridad: String?,
) : Parcelable
