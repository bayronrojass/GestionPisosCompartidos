package es.mirumi.es.ui.listas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.model.Lista
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.pizarra.postits.DraggableViewModelFactory
import es.mirumi.es.ui.pizarra.postits.PizarraScreen
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType

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

    // Estados locales
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mis listas, 1: Compartidas
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

    // Filtrar listas según la pestaña seleccionada
    val filteredListas =
        when (selectedTab) {
            0 -> listas?.filter { it.nombre !in listOf("Cena del sábado 1") } ?: emptyList()
            1 -> listas?.filter { it.nombre in listOf("Cena del sábado 1") } ?: emptyList()
            else -> emptyList()
        }

    Scaffold(
        containerColor = Color(0xfff8f8f8),
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xfff8f8f8)),
        ) {
            // Título
            Text(
                text = "Listas",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 20.dp, y = 25.dp),
            )

            // Selector de pestañas
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 65.dp, y = 105.dp)
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp),
            ) {
                // Fondo blanco
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White)
                            .shadow(4.dp, RoundedCornerShape(26.dp)),
                )

                // Fondo morado (Selector)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(if (selectedTab == 0) Alignment.CenterStart else Alignment.CenterEnd)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xffddc1fb)),
                )

                // Textos
                Text(
                    text = "Mis listas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = 22.dp)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                )
                Text(
                    text = "Compartidas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-22).dp)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }

            // Contenido principal - Listas
            when {
                isLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredListas.isEmpty() -> {
                    Text(
                        text = "No tienes más listas",
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = 400.dp),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(y = 160.dp)
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 150.dp),
                    ) {
                        items(filteredListas.size) { index ->
                            val lista = filteredListas[index]
                            val dateText =
                                when (lista.nombre) {
                                    "Compra semanal" -> "Editada el 11 de Nov"
                                    "Compra Ikea" -> "Creada el 13 de Nov"
                                    "Limpieza" -> "Creada el 13 de Nov"
                                    "Cosas hamster" -> "Creada el 13 de Nov"
                                    "Galletas caseras" -> "Editada el 23 de Oct"
                                    "Cena del sábado 1" -> "Creada el 13 de Nov"
                                    else -> "Creada el NA"
                                }

                            val participantCount =
                                when (lista.nombre) {
                                    "Compra Ikea" -> 1
                                    "Cena del sábado 1" -> 3
                                    else -> -1
                                }

                            ShoppingListItemCard(
                                title = lista.nombre,
                                date = dateText,
                                participantCount = participantCount,
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

    // Configuración del FAB y pizarra
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

// ShoppingListItem usando Card como las tareas
@Composable
fun ShoppingListItemCard(
    title: String,
    date: String,
    participantCount: Int,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp, // Sombra sutil como las tareas
            ),
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.clickable(onClick = onItemClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
        ) {
            // Información de la lista
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    style =
                        TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
                Text(
                    text = date,
                    color = Color(0xff6c6c6c),
                    style = TextStyle(fontSize = 13.sp),
                )
            }

            // Avatares de participantes y botón de eliminar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Mostrar avatares solo si hay participantes
                if (participantCount > 0) {
                    ParticipantAvatars(count = participantCount)
                }

                // Botón de eliminar
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

// Componente para los avatares de participantes - Mejorado
@Composable
fun ParticipantAvatars(count: Int) {
    Row {
        val avatarColors =
            listOf(
                Color(0xFFE57373), // Rojo claro
                Color(0xFF81C784), // Verde claro
                Color(0xFF64B5F6), // Azul claro
                Color(0xFFBA68C8), // Púrpura claro
            )

        repeat(minOf(count, avatarColors.size)) { index ->
            Box(
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(avatarColors[index])
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = CircleShape,
                        ).then(
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

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ShoppingListScreenPreview() {
    ListaScreen(viewModel(), { _, _ -> }, 0)
}
