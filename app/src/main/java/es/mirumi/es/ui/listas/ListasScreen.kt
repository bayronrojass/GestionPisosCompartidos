package es.mirumi.es.ui.listas

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.Lista
import es.mirumi.es.model.Usuario
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
    val listas by viewModel.listas.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    // Estados locales
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mis listas, 1: Compartidas
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Lista?>(null) }
    var createListName by remember { mutableStateOf("") }
    var createListDescription by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUserId = remember { sessionManager.fetchCurrentUserId() }

    LaunchedEffect(Unit) {
        viewModel.cargarListas()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // LÓGICA DE FILTRADO
    val filteredListas =
        remember(listas, selectedTab, currentUserId) {
            when (selectedTab) {
                0 -> {
                    // Mis Listas: Soy el propietario
                    listas.filter {
                        it.propietario?.id == currentUserId ||
                            (it.propietario == null) // Para listas antiguas sin propietario
                    }
                }
                1 -> {
                    // Compartidas: Soy participante pero NO propietario
                    listas.filter {
                        it.propietario != null && it.propietario.id != currentUserId
                    }
                }
                else -> emptyList()
            }
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

            // Selector de Pestañas
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 65.dp, y = 105.dp)
                        .requiredWidth(260.dp)
                        .requiredHeight(24.dp),
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

                // Fondo morado (Selección)
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

            // Lista de Elementos
            when {
                isLoading == true -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                filteredListas.isEmpty() -> {
                    Text(
                        text = "No tienes más listas",
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = 400.dp),
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
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 150.dp),
                    ) {
                        items(filteredListas.size) { index ->
                            val lista = filteredListas[index]
                            // Determinar si mostrar múltiples avatares
                            val isSharedView = selectedTab == 1 || lista.participantes.size > 1

                            ShoppingListItemCard(
                                lista = lista,
                                isShared = isSharedView,
                                currentUserId = currentUserId ?: -1,
                                onItemClick = { onNavigateToItem(lista.id, lista.nombre) },
                                onLongClick = { showDeleteDialog = lista }, // Borrar con pulsación larga
                            )
                        }
                    }
                }
            }
        }
    }

    // Lógica del FAB y Pizarra (Post-its)
    val pizarraFabActions =
        listOf(
            FabActionItem(Icons.Default.NoteAdd, "Crear Post-it", FabActionType.POST_IT),
            FabActionItem(Icons.Default.Add, "Crear Lista", FabActionType.CREAR_LISTA),
        )
    val model = viewModel<DraggableViewModel>(key = "Lista", factory = DraggableViewModelFactory("Lista", casaId))

    PizarraScreen(
        model,
        fabActions = pizarraFabActions,
        onFabActionSelected = { action ->
            when (action.action) {
                FabActionType.POST_IT -> model.addNewPostIt()
                FabActionType.CREAR_LISTA -> showCreateDialog = true
                else -> {}
            }
        },
    )

    // Diálogo Crear Lista
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
                            viewModel.crearLista(createListName, createListDescription.ifBlank { null })
                            showCreateDialog = false
                            createListName = ""
                            createListDescription = ""
                        }
                    },
                    enabled = createListName.isNotBlank(),
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // Diálogo Borrar Lista
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            },
        )
    }
}

// --- NUEVO COMPONENTE DE TARJETA ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListItemCard(
    lista: Lista,
    isShared: Boolean,
    currentUserId: Long,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Texto de fecha inteligente
    val dateText =
        remember(lista) {
            if (!lista.fechaEdicion.isNullOrBlank()) {
                "Editada el ${formatDate(lista.fechaEdicion)}"
            } else if (!lista.fechaCreacion.isNullOrBlank()) {
                "Creada el ${formatDate(lista.fechaCreacion)}"
            } else {
                "Fecha desconocida"
            }
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(15.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onItemClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(all = 20.dp),
        ) {
            // Info Izquierda
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = lista.nombre,
                    color = Color.Black,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                )
                Text(
                    text = dateText,
                    color = Color(0xff6c6c6c),
                    style = TextStyle(fontSize = 13.sp),
                )
            }

            // Avatares Derecha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.width(IntrinsicSize.Min),
            ) {
                if (isShared) {
                    // Mostrar hasta 3 participantes si es compartida
                    val participants = lista.participantes.take(3)
                    ParticipantAvatars(participants)
                } else {
                    // Mostrar solo propietario si es individual
                    val owner = lista.propietario ?: Usuario(id = currentUserId, nombre = "Yo", correo = "")
                    UserAvatar(user = owner, size = 30.dp)
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatars(participants: List<Usuario>) {
    Box(contentAlignment = Alignment.CenterEnd) {
        participants.forEachIndexed { index, user ->
            UserAvatar(
                user = user,
                size = 30.dp,
                modifier =
                    Modifier
                        .offset(x = (-20 * index).dp) // Efecto de superposición
                        .zIndex((participants.size - index).toFloat()),
            )
        }
    }
}

@Composable
fun UserAvatar(
    user: Usuario,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.LightGray)
                .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Aquí puedes cargar la imagen real si tienes la URL en el objeto Usuario
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = user.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(Color.DarkGray),
        )
    }
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        // Asume formato ISO "2023-11-13T..."
        val parts = dateString.split("T")[0].split("-")
        if (parts.size == 3) {
            val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
            "${parts[2]} de ${months[parts[1].toInt() - 1]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
