package es.mirumi.es.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Casa(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val rutaImagen: String?,
    val fechaCreacion: String,
) : Parcelable
