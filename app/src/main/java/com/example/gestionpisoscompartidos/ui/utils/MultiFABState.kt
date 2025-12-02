package com.example.gestionpisoscompartidos.ui.utils

import androidx.compose.ui.graphics.vector.ImageVector

enum class FabActionType {
    POST_IT,
    CREAR_GASTO,
    CREAR_LISTA,
    CREAR_TAREA,
    OTHER,
}

data class FabActionItem(
    val icon: ImageVector,
    var label: String,
    val action: FabActionType,
)
