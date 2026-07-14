import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.mirumi.es.R
import es.mirumi.es.model.Tarea
import kotlin.math.roundToInt

@Composable
fun TinderTaskCard(
    tarea: Tarea,
    onVote: (Boolean) -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val animationDuration = if (isDragging) 0 else 300
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(animationDuration),
        label = "",
    )
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(animationDuration),
        label = "",
    )

    // Calcular la opacidad de los sellos visuales según lo lejos que arrastres
    val likeAlpha = (offsetX / 200f).coerceIn(0f, 1f)
    val nopeAlpha = (-offsetX / 200f).coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier =
            Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(0.8f) // Proporción tipo carta de póker
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .graphicsLayer(rotationZ = animatedRotation)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            if (offsetX > 200f) {
                                onVote(true) // LIKE
                            } else if (offsetX < -200f) {
                                onVote(false) // DISLIKE
                            } else {
                                // Vuelve al centro si no deslizas lo suficiente
                                offsetX = 0f
                                rotation = 0f
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            rotation = offsetX / 15f
                        },
                    )
                },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // CONTENIDO DE LA TARJETA
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Asigna un icono dinámico según palabras clave en la tarea
                val iconRes =
                    when {
                        tarea.nombre.contains("basura", ignoreCase = true) -> R.drawable.iconocampana // Cambia por icono de basura
                        tarea.nombre.contains("comida", ignoreCase = true) -> R.drawable.iconocampana // Cambia por icono de comida
                        tarea.nombre.contains("limpia", ignoreCase = true) -> R.drawable.icono_minimizar // Cambia por escoba
                        else -> R.drawable.tareas_icon
                    }

                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Icono",
                    modifier = Modifier.size(100.dp),
                    colorFilter = ColorFilter.tint(Color(0xff581327)),
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = tarea.nombre,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )

                if (!tarea.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = tarea.descripcion!!, fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }

            // SELLO "ME GUSTA" (Verde)
            if (likeAlpha > 0f) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .border(4.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .graphicsLayer(alpha = likeAlpha, rotationZ = -15f),
                ) {
                    Text("ME GUSTA", color = Color(0xFF4CAF50), fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }

            // SELLO "LO ODIO" (Rojo)
            if (nopeAlpha > 0f) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(24.dp)
                            .border(4.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .graphicsLayer(alpha = nopeAlpha, rotationZ = 15f),
                ) {
                    Text("LO ODIO", color = Color(0xFFE53935), fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
        }
    }
}
