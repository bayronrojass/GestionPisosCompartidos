package es.mirumi.es.ui.tareas

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import es.mirumi.es.model.Tarea
import es.mirumi.es.ui.gastos.AvatarConFoto
import es.mirumi.es.ui.gastos.ColorLila
import kotlinx.coroutines.delay

@Composable
fun TaskListItem(
    tarea: Tarea,
    onCompletarClick: () -> Unit,
) {
    var mostrarCheck by remember { mutableStateOf(false) }

    // Corrutina para manejar la animación visual antes de limpiar la tarea
    LaunchedEffect(mostrarCheck) {
        if (mostrarCheck) {
            delay(800) // Muestra el check verde por 800ms
            onCompletarClick() // Dispara el evento al backend/ViewModel
            mostrarCheck = false // Resetea el estado
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            // Checkbox animado
            Box(modifier = Modifier.clickable { mostrarCheck = true }) {
                if (mostrarCheck) {
                    // Animación del check
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completada",
                        tint = Color.Green,
                        modifier = Modifier.scale(1.5f), // Pequeño efecto de escala
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pendiente",
                        tint = Color.Gray,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = tarea.nombre, modifier = Modifier.weight(1f))

            // Avatar Animado (Crossfade hace una transición suave entre caras)
            Crossfade(
                targetState = tarea.asignadoA?.fotoUrl,
                animationSpec = tween(500),
                label = "AvatarTransition", // Evita warnings de Compose
            ) { url ->
                AvatarConFoto(
                    nombre = tarea.asignadoA?.nombre ?: "?",
                    colorFondo = ColorLila,
                    fotoUrl = url,
                    size = 36.dp, // Asegúrate de darle un tamaño si lo requiere tu diseño
                )
            }
        }
    }
}
