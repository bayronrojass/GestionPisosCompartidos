package com.example.gestionpisoscompartidos.ui.item

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.model.Elemento

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    viewModel: ItemViewModel,
    casaNombre: String,
    listaNombre: String,
    navController: NavController,
) {
    val items by viewModel.items.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    // Estados para los diálogos
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Elemento?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Elemento?>(null) }
    var showItemDetail by remember { mutableStateOf<Elemento?>(null) }

    val context = LocalContext.current
    // Mostrar error si existe
    if (error != null) {
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = casaNombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navegar a perfil */ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_user),
                            contentDescription = "Perfil",
                            tint = Color.White,
                        )
                    }
                },
//                backgroundColor = Color(0xFF6200EE) // purple_500
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
//                backgroundColor = Color(0xFF6200EE)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir item",
                    tint = Color.White,
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
        ) {
            // Título de la sección
            Text(
                text = listaNombre,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.Black,
            )

            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center),
                    ) {
                        CircularProgressIndicator()
                    }
                }
                items.isEmpty() -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center),
                    ) {
                        Text(
                            text = "No hay items en esta lista.\n¡Añade uno!",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                    ) {
                        val currentListas = items

                        items(
                            count = currentListas.size,
                            key = { index -> currentListas[index].id!! },
                        ) { index ->
                            val item = currentListas[index]
                            ItemRow(
                                item = item,
                                onCompletadoClick = { viewModel.toggleItemCompletado(item) },
                                onBorrarClick = { showDeleteDialog = item },
                                onItemClick = { showItemDetail = item },
                                onEditClick = { showEditDialog = item },
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogos
    if (showAddDialog) {
        AddEditItemDialog(
            title = "Añadir Nuevo Item",
            onConfirm = { nombre, descripcion ->
                if (nombre.isNotBlank()) {
                    viewModel.crearElemento(nombre, descripcion)
                    showAddDialog = false
                } else {
                    Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showAddDialog = false },
        )
    }

    showEditDialog?.let { item ->
        AddEditItemDialog(
            title = "Editar Item",
            initialNombre = item.nombre,
            initialDescripcion = item.descripcion ?: "",
            onConfirm = { nombre, descripcion ->
                if (nombre.isNotBlank()) {
                    viewModel.actualizarNombreDescripcion(item, nombre, descripcion)
                    showEditDialog = null
                } else {
                    Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showEditDialog = null },
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

    showItemDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { showItemDetail = null },
            title = { Text(item.nombre) },
            text = { Text(item.descripcion ?: "Sin descripción.") },
            confirmButton = {
                TextButton(onClick = { showItemDetail = null }) {
                    Text("Cerrar")
                }
            },
        )
    }
}

@Composable
fun ItemRow(
    item: Elemento,
    onCompletadoClick: (Elemento) -> Unit,
    onBorrarClick: (Elemento) -> Unit,
    onItemClick: (Elemento) -> Unit,
    onEditClick: (Elemento) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox para completado
            Checkbox(
                checked = item.completado,
                onCheckedChange = { onCompletadoClick(item) },
            )

            // Información del item
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = item.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    textDecoration = if (item.completado) TextDecoration.LineThrough else TextDecoration.None,
                )
                item.descripcion?.let { descripcion ->
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = if (item.completado) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }

            // Botón de editar
            IconButton(onClick = { onEditClick(item) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }

            // Botón de borrar
            IconButton(onClick = { onBorrarClick(item) }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar")
            }
        }
    }
}

@Composable
fun AddEditItemDialog(
    title: String,
    initialNombre: String = "",
    initialDescripcion: String = "",
    onConfirm: (String, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre by remember { mutableStateOf(initialNombre) }
    var descripcion by remember { mutableStateOf(initialDescripcion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del item") },
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(nombre, descripcion.ifBlank { null })
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

@Preview
@Composable
fun ItemScreenPreview() {
    ItemScreen(viewModel(), "", "", rememberNavController())
}
