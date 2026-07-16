package es.mirumi.es.model.dtos

open class PostItDTO(
    val id: Long? = null,
    val lienzoId: Long? = null,
    val posicionX: Float = 0f,
    val posicionY: Float = 0f,
    val width: Int = 0,
    val height: Int = 0,
    val localizacion: String,
    val tipo: String = "DIBUJO",
    val rutaAudio: String? = null,
    // Hex string of the pastel background chosen from the "Color de la nota" selector
    // (e.g. `#FFF9C4`). Nullable — legacy post-its default to yellow client-side.
    val colorNota: String? = null,
)
