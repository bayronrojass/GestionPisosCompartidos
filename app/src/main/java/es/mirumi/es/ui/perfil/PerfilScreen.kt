package es.mirumi.es.ui.perfil

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import es.mirumi.es.R
import es.mirumi.es.data.SessionManager
import es.mirumi.es.model.dtos.UsuarioLogroDTO
import es.mirumi.es.ui.navigation.Route
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.CleaningServices

@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    onLogout: (String) -> Unit,
    navController: NavController,
    casaId: Long,
) {
    val viewModel: PerfilViewModel = viewModel(factory = PerfilViewModelFactory(sessionManager))
    val uiState by viewModel.uiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    // 🔥 NUEVOS ESTADOS PARA LOGROS
    var showPantallaLogros by remember { mutableStateOf(false) }
    var medallaSeleccionada by remember { mutableStateOf<UsuarioLogroDTO?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.cargarPerfil()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? -> uri?.let { viewModel.subirFotoPerfil(context, it) } }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PerfilEvent.ShowToast -> scope.launch { snackbarHostState.showSnackbar(event.message) }
                is PerfilEvent.LogoutSuccess -> onLogout(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xfff9f9f9),
    ) { paddingValues ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(25.dp))

            Text(
                "Perfil usuario",
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start).padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(30.dp))

            // TARJETA USUARIO
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                        ).clip(RoundedCornerShape(15.dp))
                        .background(Color.White)
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
                                .size(
                                    90.dp,
                                ).clip(
                                    CircleShape,
                                ).background(Color(0xFF8061A2))
                                .border(BorderStroke(2.dp, Color(0xfff8f8f8)), CircleShape)
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
                                model =
                                    coil.request.ImageRequest
                                        .Builder(
                                            LocalContext.current,
                                        ).data("$fotoUrl?v=${System.currentTimeMillis()}")
                                        .crossfade(true)
                                        .build(),
                                contentDescription = "Foto",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                loading = { CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(30.dp)) },
                                error = {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
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
                            Text(
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

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            uiState.usuario?.nombre ?: "Cargando...",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Cheff del piso",
                            color = Color(0xff585858),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top), modifier = Modifier.weight(1.2f)) {
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
                }
            }

            // GAMIFICACIÓN (PUNTOS Y BOTÓN LOGROS)
            if (uiState.gamificacion != null) {
                Spacer(modifier = Modifier.height(24.dp))

                // BANNER KARMA
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                            ).clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFF8061A2).copy(alpha = 0.15f))
                            .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFF8061A2), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Índice de Convivencia", fontSize = 14.sp, color = Color.DarkGray)
                            Text("¡Sigue así!", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Text(
                        "${uiState.gamificacion!!.puntosConvivencia} pts",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8061A2),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val medallasConseguidas = uiState.gamificacion!!.logros.count { it.estaCompletado }
                OutlinedButton(
                    onClick = { showPantallaLogros = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(55.dp),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, Color(0xFF8061A2)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF8061A2))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Ver mis ${uiState.gamificacion!!.logros.size} Logros ($medallasConseguidas completados)",
                        color = Color(0xFF8061A2),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // LISTA DE OPCIONES
            Text(
                "MI CUENTA",
                color = Color.Black,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Start).padding(horizontal = 20.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White),
            ) {
                PerfilOptionRow("Datos personales", { showEditDialog = true }, R.drawable.frame)
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)
                PerfilOptionRow("Cambiar de vivienda", { navController.navigate(Route.ListaCasas.createRoute("")) }, R.drawable.casita)
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)
                PerfilOptionRow(
                    "Datos del piso",
                    { navController.navigate(Route.GestionUsuariosPiso.createRoute(casaId)) },
                    R.drawable.iconopiso,
                )
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)
                PerfilOptionRow("Estadísticas", {}, R.drawable.iconoestadisticas)
                HorizontalDivider(color = Color(0xffe0e0e0), thickness = 1.dp)
                PerfilOptionRow("Ajustes", {}, R.drawable.iconoajustes)
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = {
                    showLogoutDialog = true
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                        ).height(
                            50.dp,
                        ),
                shape =
                    RoundedCornerShape(
                        12.dp,
                    ),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff8061a2), contentColor = Color.White),
            ) {
                Text("Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Eliminar cuenta",
                color = Color(0xff581327),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier =
                    Modifier
                        .clickable {
                            showDeleteDialog =
                                true
                        }.padding(12.dp),
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // PANTALLA DE LOGROS
    if (showPantallaLogros && uiState.gamificacion != null) {
        Dialog(
            onDismissRequest = { showPantallaLogros = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Scaffold(
                containerColor = Color(0xfff9f9f9),
                topBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { showPantallaLogros = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                        Text("Mis Logros y Medallas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
            ) { padding ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.gamificacion!!.logros) { logro ->
                        ItemMedalla(logro = logro, onClick = { medallaSeleccionada = logro })
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }

    // DIÁLOGO DE EXPLICACIÓN DE LA MEDALLA
    if (medallaSeleccionada != null) {
        val logro = medallaSeleccionada!!

        val (colorMedalla, _) = obtenerColoresNivel(logro.nivel)

        val icono =
            when (logro.categoria) {
                "TAREAS" -> Icons.Default.CleaningServices
                "ECONOMIA" -> Icons.Default.Payments
                "SOCIAL" -> Icons.Default.Forum
                else -> Icons.Default.EmojiEvents
            }

        AlertDialog(
            onDismissRequest = { medallaSeleccionada = null },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icono, contentDescription = null, tint = colorMedalla)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(logro.nombre, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            text = {
                Column {
                    Text(text = logro.descripcion, fontSize = 16.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Progreso actual: ${logro.progresoActual} / ${logro.meta}",
                        fontWeight = FontWeight.Bold,
                        color = colorMedalla,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { medallaSeleccionada = null }) {
                    Text("¡A por ello!", color = colorMedalla, fontWeight = FontWeight.Bold)
                }
            },
        )
    }

    // RESTO DE DIÁLOGOS DE LA APP
    if (showEditDialog && uiState.usuario != null) {
        var nombre by remember { mutableStateOf(uiState.usuario!!.nombre) }
        var correo by remember { mutableStateOf(uiState.usuario!!.correo) }
        AlertDialog(onDismissRequest = {
            showEditDialog = false
        }, title = { Text("Editar Perfil", color = Color.Black) }, containerColor = Color.White, text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = {
                    nombre =
                        it
                }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ; Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = correo, onValueChange = {
                    correo =
                        it
                }, label = { Text("Correo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }, confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() &&
                        correo.isNotBlank()
                    ) {
                        viewModel.actualizarPerfil(nombre, correo)
                        showEditDialog = false
                    }
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xff8061a2),
                        contentColor = Color.White,
                    ),
            ) { Text("Guardar") }
        }, dismissButton = {
            TextButton(onClick = {
                showEditDialog =
                    false
            }) { Text("Cancelar", color = Color(0xff8061a2)) }
        })
    }
    if (showLogoutDialog) {
        AlertDialog(onDismissRequest = {
            showLogoutDialog = false
        }, title = {
            Text("Cerrar Sesión", color = Color.Black)
        }, containerColor = Color.White, text = {
            Text(
                "¿Estás seguro de que quieres cerrar sesión?",
                color = Color.DarkGray,
            )
        }, confirmButton = {
            Button(onClick = {
                viewModel.cerrarSesion()
                showLogoutDialog =
                    false
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xff8061a2), contentColor = Color.White)) { Text("Sí, salir") }
        }, dismissButton = {
            TextButton(onClick = {
                showLogoutDialog =
                    false
            }) { Text("Cancelar", color = Color(0xff8061a2)) }
        })
    }
    if (showDeleteDialog) {
        AlertDialog(onDismissRequest = {
            showDeleteDialog = false
        }, title = {
            Text("¿Eliminar cuenta?", color = Color.Black)
        }, containerColor = Color.White, text = { Text("Esta acción no se puede deshacer.", color = Color.DarkGray) }, confirmButton = {
            Button(onClick = {
                viewModel.eliminarCuenta()
                showDeleteDialog =
                    false
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White)) { Text("Eliminar") }
        }, dismissButton = {
            TextButton(onClick = {
                showDeleteDialog =
                    false
            }) { Text("Cancelar", color = Color(0xff8061a2)) }
        })
    }
    if (showPhotoOptionsDialog) {
        AlertDialog(onDismissRequest = {
            showPhotoOptionsDialog = false
        }, title = { Text("Foto de Perfil") }, text = { Text("¿Qué deseas hacer con tu foto actual?") }, confirmButton = {
            Button(onClick = {
                showPhotoOptionsDialog =
                    false
                ; photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Cambiar Foto") }
        }, dismissButton = {
            TextButton(onClick = {
                showPhotoOptionsDialog =
                    false
                ; viewModel.eliminarFotoPerfil()
            }) { Text("Eliminar", color = Color.Red) }
        })
    }
}

// FUNCIÓN DE AYUDA PARA LOS COLORES
fun obtenerColoresNivel(nivel: String): Pair<Color, Color> =
    when (nivel.uppercase()) {
        "BRONCE" -> Pair(Color(0xFFCD7F32), Color(0xFFFFF7F0)) // Naranja cobrizo
        "PLATA" -> Pair(Color(0xFF9E9E9E), Color(0xFFF7F7F7)) // Gris plateado
        "ORO" -> Pair(Color(0xFFFFB300), Color(0xFFFFFBF0)) // Amarillo oro
        else -> Pair(Color(0xFF8061A2), Color(0xFFF3E5F5))
    }

// DISEÑO DE MEDALLA
@Composable
fun ItemMedalla(
    logro: UsuarioLogroDTO,
    onClick: () -> Unit,
) {
    val (colorMedalla, colorFondo) = obtenerColoresNivel(logro.nivel)
    val opacidad = if (logro.estaCompletado) 1f else 0.4f

    val icono =
        when (logro.categoria) {
            "TAREAS" -> Icons.Default.CleaningServices
            "ECONOMIA" -> Icons.Default.Payments
            "SOCIAL" -> Icons.Default.Forum
            else -> Icons.Default.EmojiEvents
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colorFondo)
                .border(
                    BorderStroke(1.dp, if (logro.estaCompletado) colorMedalla else Color.LightGray),
                    RoundedCornerShape(16.dp),
                ).clickable { onClick() }
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(colorMedalla.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorMedalla.copy(alpha = opacidad), modifier = Modifier.size(24.dp))
            if (logro.estaCompletado) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completado",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp).align(Alignment.BottomEnd),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = logro.nombre,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (logro.estaCompletado) Color.Black else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
        )
        Text(text = logro.nivel, fontSize = 11.sp, color = colorMedalla.copy(alpha = opacidad), fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(12.dp))

        val progresoRelativo = if (logro.meta > 0) (logro.progresoActual.toFloat() / logro.meta.toFloat()) else 0f
        LinearProgressIndicator(
            progress = { progresoRelativo.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = colorMedalla.copy(alpha = opacidad),
            trackColor = Color.LightGray.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "${logro.progresoActual} / ${logro.meta}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PerfilOptionRow(
    text: String,
    onClick: () -> Unit,
    iconId: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
            Text(text = text, color = Color.Black, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium))
        }
        Image(
            painter = painterResource(id = R.drawable.vector19),
            contentDescription = "Flecha",
            modifier = Modifier.size(16.dp).rotate(180f),
            colorFilter = ColorFilter.tint(Color(0xff8061a2)),
        )
    }
}
