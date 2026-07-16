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
    // Pastel background hex (`#FFF9C4`, etc.). Restored from the server so a reopened
    // post-it comes back in the color the user picked, not the default yellow.
    val colorNota: String? = null,
)
