package es.mirumi.es.ui.item

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.model.Elemento

@Composable
fun ItemScreen(
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
    var showDetailDialog by remember { mutableStateOf<Elemento?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Item")
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color(0xfff8f8f8))
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = casaNombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )

            Text(
                text = listaNombre,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF6C6C6C),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    items.isEmpty() -> {
                        Text(
                            text = "No hay elementos en esta lista",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            items(items, key = { it.id ?: it.hashCode() }) { item ->
                                Box(
                                    Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                    ),
                                ) {
                                    ItemRow(
                                        item = item,
                                        onItemClick = { showDetailDialog = item },
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
    }

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

    showDetailDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDetailDialog = null },
            title = { Text(item.nombre) },
            text = {
                val desc = item.descripcion
                Text(
                    if (desc.isNullOrBlank()) {
                        "Sin descripción."
                    } else {
                        desc
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = null }) {
                    Text("Cerrar")
                }
            },
        )
    }
}

@Composable
fun ItemRow(
    item: Elemento,
    onItemClick: () -> Unit,
    onCheckClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { onItemClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCheckClick) {
                Icon(
                    imageVector = if (item.completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (item.completado) "Marcar como pendiente" else "Marcar como completado",
                    tint = if (item.completado) Color(0xFF4CAF50) else Color.Gray,
                )
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = item.nombre,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (item.completado) TextDecoration.LineThrough else TextDecoration.None,
                        ),
                    color = if (item.completado) Color.Gray else Color.Black,
                )

                item.descripcion?.let { desc ->
                    if (desc.isNotBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Borrar",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp),
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
