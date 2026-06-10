import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector


fun getIconoTarea(categoria: String): ImageVector {
    return when (categoria.uppercase()) {
        "COCINA" -> Icons.Default.Restaurant
        "LIMPIEZA" -> Icons.Default.CleaningServices // Alternativa segura si no compila: Icons.Default.Brush
        "BAÑO" -> Icons.Default.Bathtub // Alternativa segura si no compila: Icons.Default.WaterDrop
        "COMPRAS", "COMPRA" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Assignment // Icono por defecto (una libreta de tareas)
    }
}

fun getIconoGasto(categoria: String): ImageVector {
    return when (categoria.uppercase()) {
        "ALQUILER" -> Icons.Default.Home
        "COMIDA" -> Icons.Default.RestaurantMenu
        "SUMINISTROS" -> Icons.Default.Lightbulb
        "OCIO" -> Icons.Default.Celebration
        "TRANSPORTE" -> Icons.Default.DirectionsBus
        else -> Icons.Default.AttachMoney // Icono por defecto (el símbolo del dólar/dinero)
    }
}
