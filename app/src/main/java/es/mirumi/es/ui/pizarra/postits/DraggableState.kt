package es.mirumi.es.ui.pizarra.postits

import androidx.compose.ui.geometry.Offset

data class PostItState(
    val id: Long = 0,
    val offset: Offset = Offset(0f, 0f),
    val location: String = "",
    val lienzoId: Long = 0,
    val isExpanded: Boolean = false,
    val tipo: String = "DIBUJO",
    val rutaAudio: String? = null,
)
