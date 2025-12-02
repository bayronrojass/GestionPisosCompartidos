package com.example.gestionpisoscompartidos.ui.listas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.model.Lista
import com.example.gestionpisoscompartidos.ui.pizarra.postits.DraggableViewModel
import com.example.gestionpisoscompartidos.ui.pizarra.postits.DraggableViewModelFactory
import com.example.gestionpisoscompartidos.ui.pizarra.postits.PizarraScreen
import com.example.gestionpisoscompartidos.ui.utils.FabActionItem
import com.example.gestionpisoscompartidos.ui.utils.FabActionType

@Composable
fun ListaScreen(
    viewModel: ListasViewModel,
    onNavigateToItem: (Long, String) -> Unit = { _, _ -> },
    casaId: Long,
) {
    // Estados del ViewModel
    val listas by viewModel.listas.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState()
    val error by viewModel.error.observeAsState()

    // Estados locales para diálogos
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Lista?>(null) }
    var createListName by remember { mutableStateOf("") }
    var createListDescription by remember { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarListas()
    }

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast
                .makeText(context, it, android.widget.Toast.LENGTH_LONG)
                .show()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = Color(0xfff8f8f8))
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(63.dp))

            ScreenHeader(title = "Listas")

            Spacer(modifier = Modifier.height(80.dp))

            TabSelector(
                selectedTab = 0,
                onTabSelected = { /* TODO: Implementar cambio de tab */ },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(33.dp))

            when {
                isLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                listas?.isEmpty() != false -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No hay listas creadas",
                            color = Color.Gray,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        val currentListas = listas ?: emptyList()

                        items(
                            count = currentListas.size,
                            key = { index -> currentListas[index].id }, // Usar ID como key única
                        ) { index ->
                            val lista = currentListas[index]
                            ShoppingListItem(
                                title = lista.nombre,
                                date = "Creada el NA",
                                participantCount = -1,
                                onItemClick = { onNavigateToItem(lista.id, lista.nombre) },
                                onDeleteClick = { showDeleteDialog = lista },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    val pizarraFabActions =
        listOf(
            FabActionItem(
                icon = Icons.Default.NoteAdd,
                label = "Crear Post-it",
                action = FabActionType.POST_IT,
            ),
            FabActionItem(
                icon = Icons.Default.Add,
                label = "Crear Lista",
                action = FabActionType.CREAR_LISTA,
            ),
        )
    val model = viewModel<DraggableViewModel>(key = "Lista", factory = DraggableViewModelFactory("Lista", casaId))

    PizarraScreen(
        model,
        fabActions = pizarraFabActions,
        onFabActionSelected = { action ->
            when (action.action) {
                FabActionType.POST_IT -> {
                    model.addNewPostIt()
                }
                FabActionType.CREAR_LISTA -> {
                    showCreateDialog = true
                }
                else -> {}
            }
        },
    )

    // Diálogo de creación de lista
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Nueva Lista") },
            text = {
                Column {
                    OutlinedTextField(
                        value = createListName,
                        onValueChange = { createListName = it },
                        label = { Text("Nombre de la lista*") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = createListDescription,
                        onValueChange = { createListDescription = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (createListName.isNotBlank()) {
                            viewModel.crearLista(
                                createListName,
                                createListDescription.ifBlank { null },
                            )
                            showCreateDialog = false
                            createListName = ""
                            createListDescription = ""
                        }
                    },
                    enabled = createListName.isNotBlank(),
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    // Diálogo de confirmación de borrado
    showDeleteDialog?.let { lista ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirmar Borrado") },
            text = { Text("¿Estás seguro de que quieres borrar la lista '${lista.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.borrarLista(lista)
                        showDeleteDialog = null
                    },
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

// ShoppingListItem actualizado con callbacks
@Composable
private fun ShoppingListItem(
    title: String,
    date: String,
    participantCount: Int,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(80.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(15.dp))
                .clickable { onItemClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Información de la lista
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = date,
                    color = Color(0xff6c6c6c),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Avatares de participantes y botón de eliminar
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ParticipantAvatars(count = participantCount)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar lista",
                        tint = Color.Red,
                    )
                }
            }
        }
    }
}

// Header actualizado con acciones reales (puedes personalizar)
@Composable
private fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.displaySmall,
        )

        Row {
            IconButton(onClick = { /* TODO: Acción 1 */ }) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /* TODO: Acción 2 */ }) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                )
            }
        }
    }
}

// Componente para el selector de pestañas
@Composable
private fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(24.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(26.dp)),
    ) {
        // Fondo del tab seleccionado
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .width(130.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xffddc1fb)),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            TabOption(
                text = "Mis listas",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f),
            )

            TabOption(
                text = "Compartidas",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// Componente para cada opción del tab
@Composable
private fun TabOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

// Componente para los avatares de participantes
@Composable
private fun ParticipantAvatars(count: Int) {
    Row {
        repeat(count) { index ->
            // Avatar placeholder
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.3f))
                        .then(
                            if (index > 0) {
                                Modifier.offset(x = (-8 * index).dp)
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 1165)
@Composable
private fun ShoppingListScreenPreview() {
    ListaScreen(viewModel(), { _, _ -> }, 0)
}
