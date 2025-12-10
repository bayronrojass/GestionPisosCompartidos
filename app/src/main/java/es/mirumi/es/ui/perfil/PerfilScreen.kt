package es.mirumi.es.ui.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    onLogout: (String) -> Unit,
) {
    val viewModel: PerfilViewModel =
        viewModel(
            factory = PerfilViewModelFactory(sessionManager),
        )

    val usuario by viewModel.usuario.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val toastMessage by viewModel.toastMessage.observeAsState()
    val logoutEvent by viewModel.logoutEvent.observeAsState()

    // Estados para diálogos
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Efectos
    LaunchedEffect(toastMessage) {
        toastMessage?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }
    LaunchedEffect(logoutEvent) {
        logoutEvent?.let { onLogout(it) }
    }
    LaunchedEffect(error) {
        error?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xfff9f9f9),
    ) { paddingValues ->

        // Contenedor principal con el nuevo diseño
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .background(color = Color(0xfff9f9f9))
                    .verticalScroll(rememberScrollState()),
        ) {
            // 1. TÍTULO - Posición corregida
            Text(
                text = "Perfil usuario",
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 20.dp, y = 25.dp),
            )

            // 2. TARJETA DE DATOS DEL USUARIO - Diseño exacto como en la imagen
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 20.dp, y = 100.dp)
                        .width(350.dp)
                        .clip(shape = RoundedCornerShape(15.dp))
                        .background(color = Color.White)
                        .padding(vertical = 20.dp, horizontal = 10.dp),
            ) {
                // Columna izquierda: Foto y nombre
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(120.dp),
                ) {
                    // Foto de perfil
                    Box(
                        modifier =
                            Modifier
                                .size(100.dp)
                                .clip(shape = CircleShape)
                                .background(Color(0xFF8061A2))
                                .border(
                                    border = BorderStroke(2.dp, Color(0xfff8f8f8)),
                                    shape = CircleShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = usuario?.nombre?.take(1)?.uppercase() ?: "?",
                            fontSize = 40.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = usuario?.nombre ?: "Cargando...",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = "Cheff del piso",
                            color = Color(0xff585858),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                        )
                    }
                }

                // Columna derecha: Información adicional
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    modifier = Modifier.width(150.dp),
                ) {
                    // Ubicación - con valor en negrita
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    ) {
                        Text(
                            text = "Ubicación",
                            color = Color(0xff585858),
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                        Text(
                            text = "Calle Utiel,31",
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        )
                    }

                    // Activo desde
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    ) {
                        Text(
                            text = "Activo desde",
                            color = Color(0xff585858),
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                        Text(
                            text = "Sep del 2025",
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        )
                    }

                    // Preferencia
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top),
                    ) {
                        Text(
                            text = "Preferencia",
                            color = Color(0xff585858),
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        )
                        Text(
                            text = "Silenciosa",
                            color = Color.Black,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        )
                    }
                }
            }

            // 3. TEXTO MI CUENTA
            Text(
                text = "MI CUENTA",
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 21.dp, y = 350.dp),
            )

            // 4. LISTA DE OPCIONES
            Column(
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 20.dp, y = 390.dp)
                        .width(350.dp)
                        .clip(shape = RoundedCornerShape(15.dp))
                        .background(color = Color.White),
            ) {
                // Opción 1: Datos personales
                PerfilOptionRow(
                    text = "Datos personales",
                    onClick = { showEditDialog = true },
                    iconId = R.drawable.frame,
                )

                // Separador
                HorizontalDivider(
                    color = Color(0xffe0e0e0),
                    thickness = 1.dp,
                )

                // Opción 2: Datos del piso
                PerfilOptionRow(
                    text = "Datos del piso",
                    onClick = {},
                    iconId = R.drawable.iconopiso,
                )

                // Separador
                HorizontalDivider(
                    color = Color(0xffe0e0e0),
                    thickness = 1.dp,
                )

                // Opción 3: Estadísticas
                PerfilOptionRow(
                    text = "Estadísticas",
                    onClick = {},
                    iconId = R.drawable.iconoestadisticas,
                )

                // Separador
                HorizontalDivider(
                    color = Color(0xffe0e0e0),
                    thickness = 1.dp,
                )

                // Opción 4: Ajustes
                PerfilOptionRow(
                    text = "Ajustes",
                    onClick = {},
                    iconId = R.drawable.iconoajustes,
                )
            }

            // 5. BOTÓN CERRAR SESIÓN - Posición corregida
            Button(
                onClick = { showLogoutDialog = true },
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(x = 20.dp, y = 650.dp)
                        .width(350.dp)
                        .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xff8061a2),
                    ),
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                )
            }

            // 6. TEXTO ELIMINAR CUENTA - Posición corregida
            Text(
                text = "Eliminar cuenta",
                color = Color(0xff581327),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopCenter)
                        .offset(y = 720.dp)
                        .clickable { showDeleteDialog = true }
                        .padding(12.dp),
            )

            // Espacio extra al final para el scroll
            Spacer(
                modifier =
                    Modifier
                        .height(100.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 780.dp),
            )
        }
    }

    // --- DIÁLOGOS (Misma lógica de negocio) ---

    // 1. Editar
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nombre.isNotBlank() && correo.isNotBlank()) {
                        viewModel.actualizarPerfil(nombre, correo)
                        showEditDialog = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // 2. Cerrar Sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.cerrarSesion()
                    showLogoutDialog = false
                }) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            },
        )
    }

    // 3. Eliminar Cuenta
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                ) { Text("Eliminar definitivamente") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

// --- COMPONENTES SIMPLIFICADOS ---

@Composable
fun PerfilOptionRow(
    text: String,
    onClick: () -> Unit,
    iconId: Int, // Añadimos el parámetro para el icono
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xff8061a2)),
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = text,
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            )
        }
        // Flecha
        Image(
            painter = painterResource(id = R.drawable.vector19),
            contentDescription = "Flecha",
            modifier =
                Modifier
                    .size(16.dp)
                    .rotate(180f),
            colorFilter = ColorFilter.tint(Color(0xff8061a2)),
        )
    }
}
