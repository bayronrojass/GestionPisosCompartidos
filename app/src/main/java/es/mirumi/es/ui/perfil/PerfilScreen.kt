package es.mirumi.es.ui.perfil

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.ui.navigation.Route
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    onLogout: (String) -> Unit,
    navController: NavController,
    casaId: Long,
) {
    val viewModel: PerfilViewModel =
        viewModel(
            factory = PerfilViewModelFactory(sessionManager),
        )

    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- NUEVO: Configuración para abrir la galería ---
    val context = LocalContext.current
    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            uri?.let {
                viewModel.subirFotoPerfil(context, it)
            }
        }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PerfilEvent.ShowToast -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
                is PerfilEvent.LogoutSuccess -> {
                    onLogout(event.message)
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.errorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xfff9f9f9),
    ) { paddingValues ->

        // SOLUCIÓN AL DISEÑO: Usamos un Column en lugar de un Box con offsets
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(color = Color(0xfff9f9f9))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            // Margen lateral para toda la pantalla
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(25.dp))

            // 1. TÍTULO
            Text(
                text = "Perfil usuario",
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 2. TARJETA DE DATOS DEL USUARIO
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(15.dp))
                        .background(color = Color.White)
                        .padding(vertical = 20.dp, horizontal = 10.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(90.dp)
                                .clip(shape = CircleShape)
                                .background(Color(0xFF8061A2))
                                .border(border = BorderStroke(2.dp, Color(0xfff8f8f8)), shape = CircleShape)
                                .clickable {
                                    if (!uiState.usuario?.fotoUrl.isNullOrEmpty()) {
                                        showPhotoOptionsDialog = true
                                    } else {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                        )
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        val fotoUrl = uiState.usuario?.fotoUrl

                        if (!fotoUrl.isNullOrEmpty()) {
                            SubcomposeAsyncImage(
                                model = fotoUrl,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                // MIENTRAS CARGA: Mostramos un circulito de carga infinito
                                loading = {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.padding(30.dp),
                                    )
                                },
                                // SI DA ERROR (o no carga): Mostramos la letra
                                error = {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text =
                                                uiState.usuario
                                                    ?.nombre
                                                    ?.take(1)
                                                    ?.uppercase() ?: "?",
                                            fontSize = 40.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                },
                            )
                        } else {
                            // Si el usuario no tiene foto en la BD, mostramos la letra normal
                            Text(
                                text =
                                    uiState.usuario
                                        ?.nombre
                                        ?.take(1)
                                        ?.uppercase() ?: "?",
                                fontSize = 40.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = uiState.usuario?.nombre ?: "Cargando...",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
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

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    modifier = Modifier.weight(1.2f),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)) {
                        Text("Ubicación", color = Color(0xff585858), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                        Text(
                            "Escultor José Capuz 29",
                            color = Color.Black,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)) {
                        Text("Activo desde", color = Color(0xff585858), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                        Text("Sep del 2025", color = Color.Black, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Top)) {
                        Text("Preferencia", color = Color(0xff585858), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                        Text("Silenciosa", color = Color.Black, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 3. TEXTO MI CUENTA
            Text(
                text = "MI CUENTA",
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. LISTA DE OPCIONES
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(15.dp))
                        .background(color = Color.White),
            ) {
                PerfilOptionRow(
                    text = "Datos personales",
                    onClick = { showEditDialog = true },
                    iconId = R.drawable.frame,
                )
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)

                PerfilOptionRow(
                    text = "Cambiar de vivienda",
                    onClick = { navController.navigate(Route.ListaCasas.createRoute("")) },
                    iconId = R.drawable.casita,
                )
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)

                PerfilOptionRow(
                    text = "Datos del piso",
                    onClick = { navController.navigate(Route.GestionUsuariosPiso.createRoute(casaId)) },
                    iconId = R.drawable.iconopiso,
                )
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)

                PerfilOptionRow(
                    text = "Estadísticas",
                    onClick = {},
                    iconId = R.drawable.iconoestadisticas,
                )
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)

                PerfilOptionRow(
                    text = "Ajustes",
                    onClick = {},
                    iconId = R.drawable.iconoajustes,
                )
            }

            // Espaciador dinámico: Empuja el botón hacia abajo sin montarse encima de Ajustes
            Spacer(modifier = Modifier.height(50.dp))

            // 5. BOTÓN CERRAR SESIÓN
            Button(
                onClick = { showLogoutDialog = true },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                // SOLUCIÓN COLOR: Forzamos el color del contenido (letras) a blanco puro siempre
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xff8061a2),
                        contentColor = Color.White,
                    ),
            ) {
                Text("Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. TEXTO ELIMINAR CUENTA
            Text(
                text = "Eliminar cuenta",
                color = Color(0xff581327),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier =
                    Modifier
                        .clickable { showDeleteDialog = true }
                        .padding(12.dp),
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIÁLOGOS ---

    if (showEditDialog && uiState.usuario != null) {
        var nombre by remember { mutableStateOf(uiState.usuario!!.nombre) }
        var correo by remember { mutableStateOf(uiState.usuario!!.correo) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Perfil", color = Color.Black) },
            containerColor = Color.White,
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
                Button(
                    onClick = {
                        if (nombre.isNotBlank() && correo.isNotBlank()) {
                            viewModel.actualizarPerfil(nombre, correo)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff8061a2), contentColor = Color.White),
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = Color(0xff8061a2))
                }
            },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión", color = Color.Black) },
            containerColor = Color.White,
            text = { Text("¿Estás seguro de que quieres cerrar sesión?", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cerrarSesion()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff8061a2), contentColor = Color.White),
                ) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Color(0xff8061a2))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar cuenta?", color = Color.Black) },
            containerColor = Color.White,
            text = { Text("Esta acción no se puede deshacer. Perderás acceso a tus pisos y tareas.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarCuenta()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color(0xff8061a2))
                }
            },
        )
    }

    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Foto de Perfil") },
            text = { Text("¿Qué deseas hacer con tu foto actual?") },
            confirmButton = {
                // Botón principal: Cambiarla
                Button(onClick = {
                    showPhotoOptionsDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                }) {
                    Text("Cambiar Foto")
                }
            },
            dismissButton = {
                // Botón secundario: Eliminarla (En texto rojo)
                TextButton(onClick = {
                    showPhotoOptionsDialog = false
                    viewModel.eliminarFotoPerfil()
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
        )
    }
}

@Composable
fun PerfilOptionRow(
    text: String,
    onClick: () -> Unit,
    iconId: Int,
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
        Image(
            painter = painterResource(id = R.drawable.vector19),
            contentDescription = "Flecha",
            modifier = Modifier.size(16.dp).rotate(180f),
            colorFilter = ColorFilter.tint(Color(0xff8061a2)),
        )
    }
}
