package com.example.gestionpisoscompartidos.ui.pizarra.p2

import androidx.compose.ui.geometry.Offset
import java.util.UUID

data class PostItState(
    val id: String = UUID.randomUUID().toString(),
    val offset: Offset = Offset(0f, 0f),
    val lienzoId: Long = 0,
    val isExpanded: Boolean = false,
)
