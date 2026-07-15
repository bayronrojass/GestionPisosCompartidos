package es.mirumi.es.ui.listas

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import es.mirumi.es.ui.gastos.AvatarConFoto
import es.mirumi.es.ui.gastos.ColorLila
import es.mirumi.es.ui.pizarra.postits.DraggableViewModel
import es.mirumi.es.ui.theme.Fondo
import es.mirumi.es.ui.theme.LilaLight
import es.mirumi.es.ui.theme.LilaPrimary
import es.mirumi.es.ui.theme.TextoGris
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
    val miembros by viewModel.miembros.observeAsState(emptyList())

    // Estados locales
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Mis listas, 1: Compartidas
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Lista?>(null) }
    var createListName by remember { mutableStateOf("") }
    var createListDescription by remember { mutableStateOf("") }
    var selectedParticipants by remember { mutableStateOf<List<Long>>(emptyList()) }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUserId = remember { sessionManager.fetchCurrentUserId() }

    LaunchedEffect(Unit) {
        viewModel.cargarListas()
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            viewModel.cargarMiembros(token)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // --- LÓGICA DE FILTRADO ---
    val filteredListas =
        remember(listas, selectedTab, currentUserId) {
            val allListas = listas ?: emptyList()

            // 1. PRIMER FILTRO: ¿Pertenezco a esta lista?
            val listasDondeEstoy =
                allListas.filter { lista ->
                    val soyPropietario = lista.propietario?.id == currentUserId
                    val soyParticipante = lista.participantes.any { it.id == currentUserId }
                    soyPropietario || soyParticipante
                }

            // 2. SEGUNDO FILTRO: Según la pestaña seleccionada
            when (selectedTab) {
                0 -> {
                    // Pestaña "Mis Listas" (Individuales)
                    listasDondeEstoy.filter { it.participantes.size <= 1 }
                }
                1 -> {
                    // Pestaña "Compartidas"
                    listasDondeEstoy.filter { it.participantes.size > 1 }
                }
                else -> emptyList()
            }
        }

    Scaffold(
        containerColor = Fondo,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Listas",
                color = Color.Black,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Pestañas
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
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
                            .background(LilaLight),
                )

                // Textos
                Text(
                    text = "Mis listas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterStart)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                )
                Text(
                    text = "Compartidas",
                    color = Color.Black,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 22.dp, vertical = 2.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de Elementos
            when {
                isLoading == true -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                filteredListas.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Text(
                            text = "No tienes listas en esta sección",
                            color = Color.Black,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 40.dp),
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 150.dp),
                    ) {
                        items(filteredListas.size) { index ->
                            val lista = filteredListas[index]

                            // Determinar visualización de avatares
                            val isSharedView = selectedTab == 1

                            ShoppingListItemCard(
                                lista = lista,
                                isShared = isSharedView,
                                currentUserId = currentUserId ?: -1,
                                onItemClick = { onNavigateToItem(lista.id, lista.nombre) },
                                onLongClick = { showDeleteDialog = lista },
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
    val model =
        viewModel<DraggableViewModel>(
            key = "Lista",
            factory = DraggableViewModelFactory("Lista", casaId, sessionManager),
        )

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
            onDismissRequest = {
                showCreateDialog = false
                selectedParticipants = emptyList() // Limpiar selección al cerrar
            },
            title = { Text("Crear Nueva Lista") },
            text = {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            // Hacemos scroll si hay muchos usuarios
                            .verticalScroll(rememberScrollState()),
                ) {
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
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Compartir con:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    // Componente de Selección Múltiple
                    UserMultiSelection(
                        miembros = miembros ?: emptyList(),
                        selectedIds = selectedParticipants,
                        currentUserId = currentUserId ?: -1,
                        onSelectionChanged = { selectedParticipants = it },
                    )

                    if (selectedParticipants.isEmpty()) {
                        Text(
                            text = "Lista individual (solo para ti)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (createListName.isNotBlank()) {
                            viewModel.crearLista(
                                createListName,
                                createListDescription.ifBlank { null },
                                selectedParticipants,
                                currentUserId ?: -1L,
                            )
                            showCreateDialog = false
                            createListName = ""
                            createListDescription = ""
                            selectedParticipants = emptyList()
                        }
                    },
                    enabled = createListName.isNotBlank(),
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    selectedParticipants = emptyList()
                }) { Text("Cancelar") }
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

// --- COMPONENTES VISUALES ---

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
                    color = TextoGris,
                    style = TextStyle(fontSize = 13.sp),
                )
            }

            // Avatares Derecha (Reemplaza botón borrar)
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
                    // Si el propietario es null (listas legacy), mostramos un placeholder genérico
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
                .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AvatarConFoto(
            nombre = user.nombre,
            colorFondo = ColorLila,
            fotoUrl = user.fotoUrl,
            size = size,
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

@Composable
fun UserMultiSelection(
    miembros: List<Usuario>,
    selectedIds: List<Long>,
    currentUserId: Long,
    onSelectionChanged: (List<Long>) -> Unit,
) {
    // Filtramos para no mostrar al usuario actual (ya es propietario por defecto)
    val usersToDisplay =
        remember(miembros) {
            miembros.filter { it.id != currentUserId }
        }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (usersToDisplay.isEmpty()) {
            Text("No hay otros miembros en la casa", color = Color.Gray)
        } else {
            usersToDisplay.forEach { usuario ->
                val isSelected = selectedIds.contains(usuario.id)
                UserCheckboxItem(
                    usuario = usuario,
                    isChecked = isSelected,
                    onCheckedChange = { checked ->
                        val newSelection = selectedIds.toMutableList()
                        if (checked) {
                            newSelection.add(usuario.id)
                        } else {
                            newSelection.remove(usuario.id)
                        }
                        onSelectionChanged(newSelection)
                    },
                )
            }
        }
    }
}

@Composable
fun UserCheckboxItem(
    usuario: Usuario,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onCheckedChange(!isChecked) }
                .background(if (isChecked) LilaLight.copy(alpha = 0.2f) else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isChecked) LilaLight else Color.LightGray,
                    shape = RoundedCornerShape(8.dp),
                ).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Image(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape),
                colorFilter = ColorFilter.tint(Color.Gray),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = usuario.nombre,
                style = TextStyle(fontSize = 14.sp),
            )
        }
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors =
                CheckboxDefaults.colors(
                    checkedColor = LilaPrimary,
                ),
        )
    }
}
