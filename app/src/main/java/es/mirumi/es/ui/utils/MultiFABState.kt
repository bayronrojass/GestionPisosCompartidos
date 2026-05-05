package es.mirumi.es.ui.utils

import androidx.compose.ui.graphics.vector.ImageVector

enum class FabActionType {
    POST_IT,
    AUDIO_NOTA,
    CREAR_GASTO,
    CREAR_LISTA,
    CREAR_TAREA,
    OTHER,
    ESCANEAR_TICKET,
}

data class FabActionItem(
    val icon: ImageVector,
    var label: String,
    val action: FabActionType,
)
