package es.mirumi.es.ui.pizarra.postits

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import es.mirumi.es.BuildConfig
import es.mirumi.es.R
import es.mirumi.es.ui.pizarra.PizarraView
import es.mirumi.es.ui.pizarra.PizarraViewModel
import es.mirumi.es.ui.pizarra.PizarraViewModelFactory
import es.mirumi.es.ui.utils.DynamicFloatingActionButton
import es.mirumi.es.ui.utils.FabActionItem
import es.mirumi.es.ui.utils.FabActionType
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.time.Instant
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DialogoGrabarAudio(
    onDismiss: () -> Unit,
    onAudioGrabado: (File) -> Unit,
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO)

    var estaGrabando by remember { mutableStateOf(false) }
    var archivoAudio by remember { mutableStateOf<File?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    fun startRecording() {
        val archivoTemp = File(context.cacheDir, "nota_voz_${System.currentTimeMillis()}.m4a")
        archivoAudio = archivoTemp

        mediaRecorder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(archivoTemp.absolutePath)

                try {
                    prepare()
                    start()
                    estaGrabando = true
                } catch (e: IOException) {
                    Log.e("AudioRecord", "Falló la preparación del MediaRecorder", e)
                }
            }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        estaGrabando = false
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (estaGrabando) stopRecording()
            onDismiss()
        },
        title = { Text("Grabar Nota de Voz") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (permissionState.status !is PermissionStatus.Granted) {
                    Text("Necesitamos permiso para usar el micrófono.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Dar Permiso")
                    }
                } else {
                    Text(if (estaGrabando) "Grabando..." else "Pulsa para grabar")
                    Spacer(modifier = Modifier.height(16.dp))

                    FloatingActionButton(
                        onClick = {
                            if (estaGrabando) {
                                stopRecording()
                            } else {
                                startRecording()
                            }
                        },
                        containerColor = if (estaGrabando) Color.Red else Color.DarkGray,
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = if (estaGrabando) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Grabar",
                            tint = Color.White,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (estaGrabando) stopRecording()
                    archivoAudio?.let {
                        if (it.exists() && it.length() > 0) {
                            onAudioGrabado(it)
                        }
                    }
                },
                enabled = archivoAudio != null && !estaGrabando,
            ) {
                Text("Anclar a Pizarra")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (estaGrabando) stopRecording()
                onDismiss()
            }) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
fun DraggablePostIt(
    state: PostItState,
    onDrag: (Offset) -> Unit,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onDragEnd: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .offset { IntOffset(state.offset.x.roundToInt(), state.offset.y.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDragEnd() },
                    ) { change, dragAmount ->
                        change.consume()
                        onDrag(Offset(dragAmount.x, dragAmount.y))
                    }
                }.pointerInput(state.id) {
                    detectTapGestures(onTap = { onExpandToggle() })
                }.scale(0.7f),
    ) {
        Image(
            painter = painterResource(id = R.drawable.postitplegado),
            contentDescription = "Post-it minimizado",
        )

        if (state.tipo == "AUDIO") {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Nota de voz",
                tint = Color.Black.copy(alpha = 0.5f),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .scale(2f),
            )
        }
    }
}

@Composable
fun PizarraScreen(
    viewModel: DraggableViewModel,
    fabActions: List<FabActionItem>,
    onFabActionSelected: (FabActionItem) -> Unit,
) {
    val postIts by viewModel.postIts.collectAsState()
    val expandedPostIt = postIts.find { it.isExpanded }

    var mostrarDialogoAudio by remember { mutableStateOf(false) }

    val accionesCombinadas =
        remember(fabActions) {
            val listaDefinitiva = fabActions.toMutableList()

            if (listaDefinitiva.none { it.action == FabActionType.POST_IT }) {
                listaDefinitiva.add(
                    FabActionItem(Icons.Default.NoteAdd, "Nuevo Post-it", FabActionType.POST_IT),
                )
            }
            if (listaDefinitiva.none { it.action == FabActionType.AUDIO_NOTA }) {
                listaDefinitiva.add(
                    FabActionItem(Icons.Default.Mic, "Nota de Voz", FabActionType.AUDIO_NOTA),
                )
            }
            listaDefinitiva
        }

    Box(modifier = Modifier.fillMaxSize()) {
        postIts
            .filterNot { it.isExpanded }
            .forEach { postItState ->
                DraggablePostIt(
                    state = postItState,
                    onDrag = { dragAmount ->
                        viewModel.updatePostItPosition(postItState.id, dragAmount)
                    },
                    onExpandToggle = { viewModel.toggleExpand(postItState.id) },
                    onDragEnd = { viewModel.onDragEnd(postItState.id) },
                )
            }

        DynamicFloatingActionButton(
            fabActions = accionesCombinadas,
            onFabActionSelected = { actionItem ->
                when (actionItem.action) {
                    FabActionType.POST_IT -> viewModel.addNewPostIt()
                    FabActionType.AUDIO_NOTA -> mostrarDialogoAudio = true
                    else -> onFabActionSelected(actionItem)
                }
            },
        )

        if (mostrarDialogoAudio) {
            DialogoGrabarAudio(
                onDismiss = { mostrarDialogoAudio = false },
                onAudioGrabado = { archivoFisico ->
                    mostrarDialogoAudio = false
                    viewModel.crearPostItDeAudio(archivoFisico)
                },
            )
        }

        if (expandedPostIt != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {},
                contentAlignment = Alignment.Center,
            ) {
                ExpandedPostIt(
                    onMinimize = { viewModel.toggleExpand(expandedPostIt.id) },
                    onClose = { viewModel.removePostIt(expandedPostIt.id) },
                    state = expandedPostIt,
                )
            }
        }
    }
}

@Composable
fun ExpandedPostIt(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    state: PostItState,
) {
    val pizarraViewModel: PizarraViewModel? =
        if (state.tipo == "DIBUJO") {
            viewModel(factory = PizarraViewModelFactory(state.lienzoId))
        } else {
            null
        }

    val chipBackgroundColor = Color(0xffb1395b)
    val chipContentColor = Color(0xFFFFE9EF)
    val chipColors =
        InputChipDefaults.inputChipColors(
            containerColor = chipBackgroundColor,
            labelColor = chipContentColor,
            leadingIconColor = chipContentColor,
            selectedContainerColor = chipBackgroundColor,
            selectedLabelColor = chipContentColor,
            selectedLeadingIconColor = chipContentColor,
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier =
            modifier
                .width(350.dp)
                .requiredHeight(420.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(color = Color(0xffffcddb))
                .padding(all = 20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            InputChip(
                label = { Text("Cerrar") },
                avatar = { Icon(Icons.Default.Close, "Cerrar") },
                selected = true,
                onClick = {
                    pizarraViewModel?.stop()
                    pizarraViewModel?._bitmapState?.value = null
                    onClose()
                },
                colors = chipColors,
                border = null,
            )
            InputChip(
                label = { Text("Minimizar") },
                avatar = {
                    Icon(
                        painter = painterResource(id = R.drawable.icono_minimizar),
                        contentDescription = "Minimizar",
                        modifier = Modifier.requiredSize(24.dp),
                    )
                },
                selected = true,
                onClick = {
                    pizarraViewModel?.stop()
                    pizarraViewModel?._bitmapState?.value = null
                    onMinimize()
                },
                colors = chipColors,
                border = null,
            )
            Image(
                modifier =
                    Modifier
                        .offset(0.dp, 5.dp)
                        .fillMaxWidth()
                        .requiredHeight(35.dp),
                painter = painterResource(id = R.drawable.cararosa),
                alignment = Alignment.Center,
                contentDescription = "Dibujo carita",
            )
        }

        if (state.tipo == "AUDIO" && state.rutaAudio != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(330.dp),
                contentAlignment = Alignment.Center,
            ) {
                AudioPostIt(urlAudio = state.rutaAudio)
            }
        } else {
            val stroke =
                Stroke(
                    width = 10.dp.value,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 5f), 0f),
                )
            val borderColor = Color(0xffff91b0)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(330.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .drawWithContent {
                            drawContent()
                            drawRoundRect(
                                color = borderColor,
                                style = stroke,
                                cornerRadius = CornerRadius(10.dp.toPx()),
                            )
                        },
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current

                pizarraViewModel?.let { viewModel ->
                    LaunchedEffect(viewModel, lifecycleOwner) {
                        lifecycleOwner.lifecycleScope.launch {
                            viewModel.load()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PizarraView(ctx).apply {
                                this.activatedDraw = true
                                setModel(viewModel)

                                lifecycleOwner.lifecycleScope.launch {
                                    viewModel.bitmapState.collect { bitmap ->
                                        bitmap?.let {
                                            setBackgroundBitmap(it)
                                        }
                                    }
                                }
                            }
                        },
                        update = { view ->
                            // 🔴 MAGIA AQUÍ: Solo cargamos si el post-it es NUEVO.
                            // Así evitamos que la sincronización borre tu dibujo.
                            if (viewModel.lienzoId != state.lienzoId) {
                                viewModel.lienzoId = state.lienzoId
                                viewModel.lastLoaded = Instant.ofEpochMilli(1000000)
                                view.load()
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPostIt(urlAudio: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
    val cleanPath = urlAudio.trimStart('/')
    val fullUrl = "$baseUrl/$cleanPath"

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D)),
        modifier = Modifier.size(150.dp, 150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        isPlaying = false
                    } else {
                        Log.d("AUDIO_TEST", "Intentando reproducir URL: $fullUrl")
                        try {
                            mediaPlayer =
                                MediaPlayer().apply {
                                    // 🔴 MAGIA AQUÍ: Forzamos el uso del altavoz de música al máximo volumen
                                    setAudioAttributes(
                                        android.media.AudioAttributes
                                            .Builder()
                                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                            .build(),
                                    )
                                    setVolume(1.0f, 1.0f)
                                    setDataSource(fullUrl)
                                    setOnPreparedListener {
                                        start()
                                        isPlaying = true
                                        Log.d("AUDIO_TEST", "¡Reproduciendo!")
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                        release()
                                        mediaPlayer = null
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        Log.e("AUDIO_TEST", "Error del MediaPlayer: what=$what, extra=$extra")
                                        isPlaying = false
                                        true
                                    }
                                    prepareAsync()
                                }
                        } catch (e: Exception) {
                            Log.e("AUDIO_TEST", "Excepción al configurar MediaPlayer: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.size(80.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausar Audio",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.DarkGray,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Post-it Minimizado")
@Composable
fun DraggablePostItPreview() {
    DraggablePostIt(
        state = PostItState(),
        onDrag = {},
        onExpandToggle = {},
        onDragEnd = {},
    )
}

@Preview(showBackground = true, name = "Post-it Expandido (Solo Vista)")
@Composable
fun ExpandedPostItPreview() {
    ExpandedPostIt(
        onMinimize = {},
        onClose = {},
        state = PostItState(),
    )
}
