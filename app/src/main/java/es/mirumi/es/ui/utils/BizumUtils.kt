package es.mirumi.es.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DynamicFloatingActionButton(
    fabActions: List<FabActionItem>,
    onFabActionSelected: (FabActionItem) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        when {
            fabActions.size > 1 -> {
                MultiFloatingActionButton(
                    items = fabActions,
                    onFabActionClick = onFabActionSelected,
                )
            }

            fabActions.size == 1 -> {
                val singleAction = fabActions.first()
                singleAction.label = ""
                FloatingActionButton(
                    shape = CircleShape,
                    containerColor = Color.Black,
                    onClick = { onFabActionSelected(singleAction) },
                ) {
                    Icon(
                        imageVector = singleAction.icon,
                        contentDescription = singleAction.label,
                        tint = Color.White,
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
fun MiniFabItem(
    item: FabActionItem,
    onFabItemClick: (FabActionItem) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (item.label.isNotBlank()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Text(
                    text = item.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        SmallFloatingActionButton(
            onClick = { onFabItemClick(item) },
            shape = CircleShape,
            containerColor = Color.Black,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color.White,
            )
        }
    }
}

@Composable
fun MultiFloatingActionButton(
    items: List<FabActionItem>,
    onFabActionClick: (FabActionItem) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f)

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items.forEach { item ->
                    MiniFabItem(
                        item = item,
                        onFabItemClick = {
                            isExpanded = false
                            onFabActionClick(it)
                        },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            shape = CircleShape,
            containerColor = Color.Black,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Abrir menú de acciones",
                tint = Color.White,
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Preview(name = "1. MiniFAB Item", showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun PreviewMiniFabItem() {
    MaterialTheme {
        MiniFabItem(
            item =
                FabActionItem(
                    icon = Icons.Default.NoteAdd,
                    label = "Crear Post-it",
                    action = FabActionType.POST_IT,
                ),
            onFabItemClick = {},
        )
    }
}

@Preview(name = "2. MultiFAB Plegado", showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun PreviewMultiFabCollapsed() {
    MaterialTheme {
        MultiFloatingActionButton(
            items =
                listOf(
                    FabActionItem(Icons.Default.NoteAdd, "Post-it", FabActionType.POST_IT),
                ),
            onFabActionClick = {},
        )
    }
}

@Preview(name = "3. MultiFAB Desplegado", showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun PreviewMultiFabExpanded() {
    @Composable
    fun MultiFabForPreview(initiallyExpanded: Boolean) {
        var isExpanded by remember { mutableStateOf(initiallyExpanded) }
        val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f)

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MiniFabItem(
                        item =
                            FabActionItem(
                                Icons.Default.NoteAdd,
                                "Crear Post-it",
                                FabActionType.POST_IT,
                            ),
                        onFabItemClick = { isExpanded = false },
                    )
                    MiniFabItem(
                        item =
                            FabActionItem(
                                Icons.Default.Edit,
                                "Crear Tarea",
                                FabActionType.OTHER,
                            ),
                        onFabItemClick = { isExpanded = false },
                    )
                }
            }
            FloatingActionButton(
                shape = CircleShape,
                containerColor = Color.Black,
                onClick = { isExpanded = !isExpanded },
            ) {
                Icon(
                    Icons.Default.Add,
                    "Abrir menú",
                    modifier = Modifier.rotate(rotation),
                    tint = Color.White,
                )
            }
        }
    }

    MaterialTheme {
        MultiFabForPreview(initiallyExpanded = true)
    }
}
