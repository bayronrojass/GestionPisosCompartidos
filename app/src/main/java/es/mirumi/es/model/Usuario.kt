package es.mirumi.es.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    @SerializedName("id")
    val id: Long,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("fotoUrl")
    val fotoUrl: String? = null,
) : Parcelable
