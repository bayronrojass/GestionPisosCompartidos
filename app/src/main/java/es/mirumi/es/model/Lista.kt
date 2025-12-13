package es.mirumi.es.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Lista(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val fechaCreacion: String? = null,
    val fechaEdicion: String? = null,
    val propietario: Usuario? = null,
    val participantes: List<Usuario> = emptyList(),
) : Parcelable
