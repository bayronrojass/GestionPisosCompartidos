package es.mirumi.es.model.dtos

class ImagenDTO(
    id: Long,
    lienzoId: Long,
    posicionX: Float,
    posicionY: Float,
    width: Int,
    height: Int,
    localizacion: String,
) : PostItDTO(id, lienzoId, posicionX, posicionY, width, height, localizacion)
