package com.example.gestionpisoscompartidos.ui.pizarra.p2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.R
import kotlin.math.roundToInt

@Composable
fun DraggablePostIt(
    state: PostItState,
    onDrag: (Offset) -> Unit,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = R.drawable.home_union),
        contentDescription = "Post-it minimizado",
        modifier =
            modifier
                .offset { IntOffset(state.offset.x.roundToInt(), state.offset.y.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(Offset(dragAmount.x, dragAmount.y))
                    }
                }.pointerInput(state.id) {
                    detectTapGestures(onTap = { onExpandToggle() })
                },
    )
}

@Composable
fun PizarraScreen(
    viewModel: DraggableViewModel = viewModel(),
    location: String,
) {
    val postIts by viewModel.postIts.collectAsState()
    val expandedPostIt = postIts.find { it.isExpanded }

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
                )
            }

        FloatingActionButton(
            onClick = { viewModel.addNewPostIt() },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Añadir Post-it")
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
                    onClose = { viewModel.removePostIt(expandedPostIt.id) },
                    state = PostItState(lienzoId = 1L),
                )
            }
        }
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, name = "Post-it Minimizado")
@Composable
fun DraggablePostItPreview() {
    DraggablePostIt(
        state = PostItState(),
        onDrag = {},
        onExpandToggle = {},
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
