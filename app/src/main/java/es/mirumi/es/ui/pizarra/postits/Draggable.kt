package es.mirumi.es.ui.pizarra.postits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import es.mirumi.es.R
import es.mirumi.es.ui.utils.DynamicFloatingActionButton
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType
import kotlin.math.roundToInt

@Composable
fun DraggablePostIt(
    state: PostItState,
    onDrag: (Offset) -> Unit,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onDragEnd: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .offset { IntOffset(state.offset.x.roundToInt(), state.offset.y.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDragEnd() },
                    ) { change, dragAmount ->
                        change.consume()
                        onDrag(Offset(dragAmount.x, dragAmount.y))
                    }
                }.pointerInput(state.id) {
                    detectTapGestures(onTap = { onExpandToggle() })
                }.scale(0.7f),
    ) {
        Image(
            painter = painterResource(id = R.drawable.postitplegado),
            contentDescription = "Post-it minimizado",
        )

        // 🔴 DETALLE VISUAL: Si es de audio, pintamos un micro en el post-it
        if (state.tipo == "AUDIO") {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Nota de voz",
                tint = Color.Black.copy(alpha = 0.5f),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .scale(2f),
            )
        }
    }
}

@Composable
fun PizarraScreen(
    viewModel: DraggableViewModel,
    fabActions: List<FabActionItem>,
    onFabActionSelected: (FabActionItem) -> Unit,
) {
    val postIts by viewModel.postIts.collectAsState()
    val expandedPostIt = postIts.find { it.isExpanded }

    var mostrarDialogoAudio by remember { mutableStateOf(false) }

    // 🔴 MAGIA AQUÍ: Fusionamos las acciones de la pantalla (ej. "Crear Tarea")
    // con las acciones nativas de la pizarra ("Post-it" y "Audio")
    val accionesCombinadas =
        remember(fabActions) {
            val listaDefinitiva = fabActions.toMutableList()

            // Si la pantalla no incluyó el Post-it, lo forzamos a aparecer
            if (listaDefinitiva.none { it.action == FabActionType.POST_IT }) {
                listaDefinitiva.add(
                    FabActionItem(Icons.Default.NoteAdd, "Nuevo Post-it", FabActionType.POST_IT),
                )
            }
            // Inyectamos el micrófono si no está
            if (listaDefinitiva.none { it.action == FabActionType.AUDIO_NOTA }) {
                listaDefinitiva.add(
                    FabActionItem(Icons.Default.Mic, "Nota de Voz", FabActionType.AUDIO_NOTA),
                )
            }
            listaDefinitiva
        }

    Box(modifier = Modifier.fillMaxSize()) {
        postIts
            .filterNot { it.isExpanded }
            .forEach { postItState ->
                DraggablePostIt(
                    state = postItState,
                    onDrag = { dragAmount ->
                        viewModel.updatePostItPosition(postItState.id, dragAmount)
                    },
                    onExpandToggle = { viewModel.toggleExpand(postItState.id) },
                    onDragEnd = { viewModel.onDragEnd(postItState.id) },
                )
            }

        // Le pasamos nuestra nueva lista combinada
        DynamicFloatingActionButton(
            fabActions = accionesCombinadas,
            onFabActionSelected = { actionItem ->
                // 🔴 LA PIZARRA GESTIONA SUS PROPIOS BOTONES 🔴
                when (actionItem.action) {
                    FabActionType.POST_IT -> {
                        viewModel.addNewPostIt()
                    }
                    FabActionType.AUDIO_NOTA -> {
                        mostrarDialogoAudio = true
                    }
                    else -> {
                        // Si es otra cosa (ej. "Crear Tarea"), se lo pasamos a la pantalla padre
                        onFabActionSelected(actionItem)
                    }
                }
            },
        )

        // DIÁLOGO DEL MICRÓFONO
        if (mostrarDialogoAudio) {
            DialogoGrabarAudio(
                onDismiss = { mostrarDialogoAudio = false },
                onAudioGrabado = { archivoFisico ->
                    mostrarDialogoAudio = false
                    viewModel.crearPostItDeAudio(archivoFisico)
                },
            )
        }

        if (expandedPostIt != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {},
                contentAlignment = Alignment.Center,
            ) {
                ExpandedPostIt(
                    onMinimize = { viewModel.toggleExpand(expandedPostIt.id) },
                    onClose = {
                        viewModel.removePostIt(expandedPostIt.id)
                    },
                    state = expandedPostIt,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Post-it Minimizado")
@Composable
fun DraggablePostItPreview() {
    DraggablePostIt(
        state = PostItState(),
        onDrag = {},
        onExpandToggle = {},
        onDragEnd = {},
    )
}

@Preview(showBackground = true, name = "Post-it Expandido (Solo Vista)")
@Composable
fun ExpandedPostItPreview() {
    ExpandedPostIt(
        onMinimize = {},
        onClose = {},
        state = PostItState(),
    )
}
