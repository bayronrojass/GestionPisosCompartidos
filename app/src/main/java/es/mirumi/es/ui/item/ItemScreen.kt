package es.mirumi.es.ui.item

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.model.Elemento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.input.ImeAction

@Composable
fun ItemScreen(
    navController: NavController,
    listaId: Long,
    listaNombre: String,
    casaNombre: String,
    viewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(listaId)),
) {
    val context = LocalContext.current

    val items by viewModel.items.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Elemento?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Elemento?>(null) }

    // Estado para el nuevo item
    var nuevoItemNombre by remember { mutableStateOf("") }
    // Estado para cantidades
    val cantidades = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(items) {
        // Asegurar que cada item tenga una cantidad inicial
        items.forEach { item ->
            if (item.id != null && !cantidades.containsKey(item.id)) {
                cantidades[item.id] = "1"
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            // Barra superior
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White)
                        .shadow(elevation = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Botón de retroceso
                Box(
                    modifier =
                        Modifier
                            .padding(start = 16.dp, top = 50.dp)
                            .size(56.dp)
                            .clickable { navController.navigateUp() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver atrás",
                        tint = Color(0xff6c6c6c),
                        modifier = Modifier.size(32.dp),
                    )
                }

                // Título "Lista" alineado a la derecha
                Text(
                    text = "Lista",
                    color = Color(0xff6c6c6c),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 72.dp, top = 50.dp),
                    textAlign = TextAlign.End,
                )
            }
        },
        floatingActionButton = {
            // Botón flotante verde
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF4A7A4A),
                modifier =
                    Modifier
                        .size(60.dp)
                        .padding(bottom = 16.dp, end = 16.dp),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir Item",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xfff8f8f8))
                    .padding(paddingValues),
        ) {
            // Título de la lista y fecha
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val nombreLimpio = listaNombre.replace("+", " ").trim()
                Text(
                    text = nombreLimpio,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                )

                Text(
                    text = obtenerFechaActual(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xff6c6c6c),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            // Campo para añadir nuevo ítem
            Card(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(15.dp),
                        ),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Campo "Nuevo"
                    OutlinedTextField(
                        value = nuevoItemNombre,
                        onValueChange = { nuevoItemNombre = it },
                        placeholder = {
                            Text(
                                "Nuevo",
                                color = Color(0xff6c6c6c),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 18.sp,
                            )
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                        singleLine = true,
                        textStyle =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                            ),
                    )

                    // Botón + para añadir
                    IconButton(
                        onClick = {
                            if (nuevoItemNombre.isNotBlank()) {
                                viewModel.crearElemento(nuevoItemNombre, null)
                                nuevoItemNombre = ""
                            }
                        },
                        modifier = Modifier.size(40.dp),
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White,
                            ),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Añadir",
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // Lista de elementos
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    items.isEmpty() -> {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "No tienes más artículos",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xff6c6c6c),
                                fontSize = 16.sp,
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp),
                        ) {
                            items(items, key = { it.id ?: it.hashCode() }) { item ->
                                ItemRowEstiloLista(
                                    item = item,
                                    cantidad = cantidades[item.id] ?: "1",
                                    onCantidadChange = { nuevaCantidad ->
                                        if (item.id != null) {
                                            // Validar que sea un número positivo
                                            if (nuevaCantidad.matches(Regex("\\d+")) || nuevaCantidad.isEmpty()) {
                                                cantidades[item.id] = nuevaCantidad
                                            }
                                        }
                                    },
                                    onIncreaseClick = {
                                        if (item.id != null) {
                                            val current = cantidades[item.id]?.toIntOrNull() ?: 1
                                            cantidades[item.id] = (current + 1).toString()
                                        }
                                    },
                                    onDecreaseClick = {
                                        if (item.id != null) {
                                            val current = cantidades[item.id]?.toIntOrNull() ?: 1
                                            if (current > 1) {
                                                cantidades[item.id] = (current - 1).toString()
                                            }
                                        }
                                    },
                                    onCheckClick = { viewModel.toggleItemCompletado(item) },
                                    onEditClick = { showEditDialog = item },
                                    onDeleteClick = { showDeleteDialog = item },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (showCreateDialog) {
        ItemDialog(
            title = "Añadir Nuevo Item",
            onDismiss = { showCreateDialog = false },
            onConfirm = { nombre, descripcion ->
                viewModel.crearElemento(nombre, descripcion)
                showCreateDialog = false
            },
        )
    }

    showEditDialog?.let { item ->
        ItemDialog(
            title = "Editar Item",
            initialName = item.nombre,
            initialDescription = item.descripcion ?: "",
            onDismiss = { showEditDialog = null },
            onConfirm = { nombre, descripcion ->
                viewModel.actualizarNombreDescripcion(item, nombre, descripcion)
                showEditDialog = null
            },
        )
    }

    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Borrar Item") },
            text = { Text("¿Seguro que quieres borrar '${item.nombre}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarElemento(item)
                        showDeleteDialog = null
                    },
                ) { Text("Borrar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
fun ItemRowEstiloLista(
    item: Elemento,
    cantidad: String,
    onCantidadChange: (String) -> Unit,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit,
    onCheckClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ).clip(RoundedCornerShape(15.dp)),
        colors =
            CardDefaults.cardColors(
                containerColor = if (item.completado) Color(0xFFE8F5E9) else Color.White,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Checkbox a la izquierda
            IconButton(
                onClick = onCheckClick,
                modifier = Modifier.size(32.dp),
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (item.completado) Color(0xFF4CAF50) else Color(0xff6c6c6c),
                    ),
            ) {
                if (item.completado) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Completado",
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    Icon(
                        Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Pendiente",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            // Nombre del producto (se expande)
            Text(
                text = item.nombre,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        textDecoration = if (item.completado) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                color = if (item.completado) Color(0xff6c6c6c) else Color.Black,
                modifier = Modifier.weight(1f),
            )

            // Controles de cantidad (como en la imagen)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Botón -
                IconButton(
                    onClick = onDecreaseClick,
                    modifier = Modifier.size(32.dp),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color(0xff6c6c6c),
                        ),
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Disminuir cantidad",
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Campo de cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = onCantidadChange,
                    modifier = Modifier.width(60.dp),
                    textStyle =
                        androidx.compose.ui.text.TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    singleLine = true,
                    // CORREGIDO: Usando KeyboardOptions directamente
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                        ),
                )

                // Botón +
                IconButton(
                    onClick = onIncreaseClick,
                    modifier = Modifier.size(32.dp),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                        ),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aumentar cantidad",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDialog(
    title: String,
    initialName: String = "",
    initialDescription: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
) {
    var nombre by remember { mutableStateOf(initialName) }
    var descripcion by remember { mutableStateOf(initialDescription) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        isError = false
                    },
                    label = { Text("Nombre del item") },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                        ),
                )
                if (isError) {
                    Text(
                        "El nombre no puede estar vacío",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(nombre, descripcion.ifBlank { null })
                    } else {
                        isError = true
                    }
                },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

// Función auxiliar para obtener la fecha actual formateada en español
@Composable
fun obtenerFechaActual(): String {
    val calendar = Calendar.getInstance()
    val fecha = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return fecha.format(calendar.time).replaceFirstChar {
        it.titlecase(Locale("es", "ES"))
    }
}
