package es.mirumi.es.ui.item

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.mirumi.es.R
import es.mirumi.es.model.Elemento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Definición de colores del diseño
val GrayText = Color(0xFF6C6C6C)
val BackgroundGray = Color(0xFFF8F8F8)
val BlackText = Color.Black

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

    var showEditDialog by remember { mutableStateOf<Elemento?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Elemento?>(null) }

    val itemsOrdenados =
        remember(items) {
            items.sortedWith(
                compareBy<Elemento> { it.completado }
                    .thenBy { it.id },
            )
        }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        containerColor = BackgroundGray,
        topBar = {
            CustomTopBar(
                title = "Lista",
                onBackClick = { navController.navigateUp() },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Lógica de Post-it */ },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .size(60.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.iconoapp),
                    contentDescription = "Post-its",
                    modifier = Modifier.size(30.dp),
                    tint = Color.White,
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Título y Fecha
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = listaNombre.replace("+", " "),
                    style =
                        TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlackText,
                        ),
                )
                Text(
                    text = obtenerFechaActual(),
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            color = GrayText,
                        ),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            // Input para añadir nuevo elemento
            AddItemRow(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                onAddItem = { nombre ->
                    viewModel.crearElemento(nombre, null)
                },
            )

            // Lista de Elementos
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF581327))
                }
            } else if (itemsOrdenados.isEmpty()) {
                Text(
                    text = "No tienes más artículos",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                    textAlign = TextAlign.Center,
                    color = GrayText,
                    fontSize = 14.sp,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(itemsOrdenados, key = { it.id ?: it.hashCode() }) { item ->
                        ItemRow(
                            item = item,
                            onToggleComplete = { viewModel.toggleItemCompletado(item) },
                            onQuantityChange = { newQty ->
                                viewModel.actualizarCantidad(item, newQty)
                            },
                            onEditRequest = { showEditDialog = item },
                            onDeleteRequest = { showDeleteDialog = item },
                        )
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---
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
                TextButton(onClick = {
                    viewModel.borrarElemento(item)
                    showDeleteDialog = null
                }) {
                    Text("Borrar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            },
        )
    }
}

// ==========================================
// COMPONENTES UI
// ==========================================

@Composable
fun AddItemRow(
    modifier: Modifier = Modifier,
    onAddItem: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp)),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = "Nuevo",
                        color = GrayText.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(fontSize = 16.sp, color = BlackText),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(onDone = {
                            if (text.isNotBlank()) {
                                onAddItem(text)
                                text = ""
                                focusManager.clearFocus()
                            }
                        }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onAddItem(text)
                        text = ""
                        focusManager.clearFocus()
                    }
                },
                modifier =
                    Modifier
                        .size(24.dp)
                        .border(1.dp, GrayText, CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Añadir",
                    tint = GrayText,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
fun ItemRow(
    item: Elemento,
    onToggleComplete: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onEditRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val backgroundColor = if (item.completado) Color(0xFFE8F5E9) else Color.White
    val textColor = if (item.completado) GrayText else BlackText

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))
                .clickable { onEditRequest() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (item.completado) Color(0xFF4A7A4A) else GrayText,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable { onToggleComplete() },
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.nombre,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (item.completado) TextDecoration.LineThrough else null,
                        ),
                    color = textColor,
                    maxLines = 1,
                )
            }

            if (!item.completado) {
                QuantityController(
                    quantity = item.cantidad,
                    onQuantityChange = onQuantityChange,
                )
            } else {
                IconButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = GrayText,
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityController(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    var textValue by remember(quantity) { mutableStateOf(quantity.toString()) }
    val focusManager = LocalFocusManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .border(1.dp, GrayText, CircleShape)
                    .clip(CircleShape)
                    .clickable { if (quantity > 1) onQuantityChange(quantity - 1) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Remove, "Menos", tint = GrayText, modifier = Modifier.size(16.dp))
        }

        Box(
            modifier =
                Modifier
                    .width(40.dp)
                    .border(1.dp, GrayText.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) textValue = newValue
                },
                textStyle =
                    TextStyle(
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = GrayText,
                        fontWeight = FontWeight.Medium,
                    ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(onDone = {
                        val intValue = textValue.toIntOrNull() ?: 1
                        if (intValue > 0) onQuantityChange(intValue) else textValue = quantity.toString()
                        focusManager.clearFocus()
                    }),
                singleLine = true,
                modifier =
                    Modifier
                        .wrapContentWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                val intValue = textValue.toIntOrNull() ?: 1
                                if (intValue != quantity && intValue > 0) {
                                    onQuantityChange(intValue)
                                }
                            }
                        },
            )
        }

        Box(
            modifier =
                Modifier
                    .size(28.dp)
                    .border(1.dp, GrayText, CircleShape)
                    .clip(CircleShape)
                    .clickable { onQuantityChange(quantity + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Add, "Más", tint = GrayText, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CustomTopBar(
    title: String,
    onBackClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(BackgroundGray)
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBackClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Atrás",
                tint = GrayText,
            )
        }

        Text(
            text = title,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = GrayText),
            modifier = Modifier.align(Alignment.CenterEnd),
        )

        Divider(
            color = GrayText.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun obtenerFechaActual(): String {
    val calendar = Calendar.getInstance()
    val fecha = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    return fecha.format(calendar.time).replaceFirstChar { it.titlecase(Locale("es", "ES")) }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
            }
        },
        confirmButton = {
            Button(onClick = { if (nombre.isNotBlank()) onConfirm(nombre, descripcion) }) { Text("Guardar") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancelar") } },
    )
}
