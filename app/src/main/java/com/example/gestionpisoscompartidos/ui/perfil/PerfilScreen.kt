package com.example.gestionpisoscompartidos.ui.perfil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.data.SessionManager

@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
) {
    val viewModel: PerfilViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = PerfilViewModelFactory(sessionManager),
        )

    val usuario by viewModel.usuario.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val navEvent by viewModel.navigationEvent.observeAsState()
    val toastMessage by viewModel.toastMessage.observeAsState()

    val context = LocalContext.current

    // Estados para diálogos
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // Navegación tras borrar cuenta o logout
    LaunchedEffect(navEvent) {
        if (navEvent == "Login") {
            onLogout()
        }
    }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Avatar Grande
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier =
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
            tint = Color.White,
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // UT: Ver Perfil (Mostrar Datos)
            usuario?.let { user ->
                Text(
                    text = user.nombre,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user.correo,
                    fontSize = 16.sp,
                    color = Color.Gray,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón Editar Perfil
        Button(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Cerrar Sesión
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Eliminar Cuenta
        TextButton(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
        ) {
            Text("Eliminar Cuenta")
        }
    }

    // --- DIÁLOGO: EDITAR PERFIL ---
    if (showEditDialog && usuario != null) {
        var nombre by remember { mutableStateOf(usuario!!.nombre) }
        var correo by remember { mutableStateOf(usuario!!.correo) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Perfil") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombre.isNotBlank() && correo.isNotBlank()) {
                            viewModel.actualizarPerfil(nombre, correo)
                            showEditDialog = false
                        }
                    },
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // --- DIÁLOGO: ELIMINAR CUENTA ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar cuenta?") },
            text = { Text("Esta acción no se puede deshacer. Perderás acceso a tus pisos y tareas.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarCuenta()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // --- DIÁLOGO: CERRAR SESIÓN ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cerrarSesion()
                        showLogoutDialog = false
                    },
                ) { Text("Cerrar Sesión") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            },
        )
    }
}
