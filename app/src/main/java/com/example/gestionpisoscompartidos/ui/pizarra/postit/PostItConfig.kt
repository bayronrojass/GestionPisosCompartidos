package com.example.gestionpisoscompartidos.ui.pizarra.postit

data class PostItConfig(
    val x: Float = 100f,
    val y: Float = 100f,
    val casaId: Long,
    val isImage: Boolean,
    var width: Int = 300,
    var height: Int = 300,
    val expansionScale: Float = 0.6f,
    val expansionHeightScale: Float = 0.25f,
    val animationDuration: Long = 300L,
)
