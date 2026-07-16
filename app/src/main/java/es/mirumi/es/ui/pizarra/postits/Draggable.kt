package es.mirumi.es.ui.pizarra.postits

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import es.mirumi.es.BuildConfig
import es.mirumi.es.R
import es.mirumi.es.data.repository.repositories.RepositoryPostIt
import es.mirumi.es.model.Usuario
import es.mirumi.es.ui.pizarra.PizarraView
import es.mirumi.es.ui.pizarra.PizarraViewModel
import es.mirumi.es.ui.pizarra.PizarraViewModelFactory
import es.mirumi.es.ui.theme.ButtonShape
import es.mirumi.es.ui.theme.Burgundy
import es.mirumi.es.ui.theme.LilaLight
import es.mirumi.es.ui.theme.LilaPrimary
import es.mirumi.es.ui.theme.TextoGris
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
    // Observe the members map so the "Enviar nota a" chips in ExpandedPostIt render
    // the real house members (Daniel, Natalia, Raquel, Marta, ...) instead of the
    // "Cargando miembros…" placeholder.
    val miembros by viewModel.miembros.collectAsState()

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
                    members = miembros,
                    onColorNotaChanged = { hex ->
                        viewModel.updatePostItColorLocal(expandedPostIt.id, hex)
                    },
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
    members: List<Usuario> = emptyList(),
    // Called the instant the user picks a new pastel — the parent screen uses this to
    // optimistically update the `_postIts` StateFlow so a subsequent close/reopen (before
    // the 60s `syncPostIts` tick catches up) sees the freshly-picked color rather than
    // reverting to the stale server value (which would show as yellow on first pick).
    onColorNotaChanged: (String) -> Unit = {},
) {
    val pizarraViewModel: PizarraViewModel? =
        if (state.tipo == "DIBUJO") {
            viewModel(factory = PizarraViewModelFactory(state.lienzoId))
        } else {
            null
        }

    // Hoisted UI state for the new control panel.
    //
    // `noteColor` — initialized from the server-persisted `state.colorNota` (via
    // `parseNoteColor`), falling back to the classic pastel yellow when the field is
    // null / blank / unparseable. This restores the user's chosen pastel across app
    // restarts and re-opens (was previously stuck on yellow because the `remember`
    // initializer was `NOTE_PASTELS.first()` unconditionally). The `remember` key
    // includes `state.colorNota` too so a background sync landing a fresh color updates
    // the local pick without needing a full state.id change.
    //
    // `brushByte` — pushed straight into `PizarraViewModel.color` so `createPaint`
    // picks the right byte on the next ACTION_DOWN.
    //
    // `assignedUserId` — in-memory only for this pass (backend assignee relation is a
    // follow-up once the schema adds `PostIt.asignadoA: Usuario?`).
    var noteColor by remember(state.id, state.colorNota) {
        mutableStateOf(parseNoteColor(state.colorNota) ?: NOTE_PASTELS.first())
    }
    var brushByte by remember(state.id) { mutableStateOf(6.toByte()) }
    var assignedUserId by remember(state.id) { mutableStateOf<Long?>(null) }
    // Handle on the AndroidView-backed PizarraView so the "Borrar" control-panel button
    // can call `.clearCanvas()` imperatively.
    var pizarraViewRef by remember(state.id) { mutableStateOf<PizarraView?>(null) }

    // Coroutine scope for firing the `PUT /postits/{id}/color-nota` persistence call
    // when the user picks a new pastel. Lifetime is tied to the composable — the call
    // will be cancelled cleanly if the user minimizes mid-request. That's fine for
    // this fire-and-forget update: the color is already applied locally, and the
    // server side is idempotent for repeat submissions.
    val persistenceScope = rememberCoroutineScope()
    val postItRepository = remember { RepositoryPostIt() }

    // Keep the ViewModel's brush byte in lock-step with the selector. Runs on every
    // brushByte change (including initial) so `createPaint` inside `PizarraView` picks
    // the right color from the palette on the next stroke.
    LaunchedEffect(pizarraViewModel, brushByte) {
        pizarraViewModel?.color = brushByte
    }

    // Pill button style — matches the burgundy-header spec from the design mockup.
    val chipColors =
        InputChipDefaults.inputChipColors(
            containerColor = Burgundy,
            labelColor = Color.White,
            leadingIconColor = Color.White,
            selectedContainerColor = Burgundy,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White,
        )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        modifier =
            modifier
                .width(360.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(color = noteColor)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                        modifier = Modifier.requiredSize(20.dp),
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
            // Smiley face pinned to the top-right — matches both mockup variants.
            Image(
                modifier =
                    Modifier
                        .weight(1f)
                        .requiredHeight(35.dp),
                painter = painterResource(id = R.drawable.cararosa),
                alignment = Alignment.CenterEnd,
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
                    // Fast-path hydration on entry. Skips the `isUpdated` RTT that the poll
                    // cycle would pay first, halving the initial network cost. Idempotent
                    // with `view.load()` below — both converge on the same shared bitmap
                    // fetch inside the ViewModel.
                    LaunchedEffect(viewModel, lifecycleOwner) {
                        lifecycleOwner.lifecycleScope.launch {
                            viewModel.initialLoad()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PizarraView(ctx).apply {
                                this.activatedDraw = true
                                setModel(viewModel)
                                // Capture the view instance so the control panel's Borrar
                                // button can call clearCanvas() imperatively.
                                pizarraViewRef = this

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
                            // Solo cargamos si el post-it es NUEVO — evita que la
                            // sincronización borre tu dibujo local si vuelves a un post-it
                            // ya abierto.
                            if (viewModel.lienzoId != state.lienzoId) {
                                viewModel.lienzoId = state.lienzoId
                                viewModel.lastLoaded = Instant.ofEpochMilli(1000000)
                                view.load()
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Async-hydration spinner overlay — visible only while `_isLoading` is
                    // true (initial hydration in flight). Sits on top of the still-empty
                    // canvas so the user gets an immediate signal that content is coming,
                    // instead of a blank 3-4s stall on Post-It open. Once the ViewModel
                    // emits the fetched bitmap, `isLoading` flips false and the spinner
                    // disappears, revealing the freshly-loaded canvas underneath.
                    val isLoading by viewModel.isLoading.collectAsState()
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = LilaPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }
                }
            }

            // Bottom control panel — matches the "Crear post it" mockup: note color +
            // brush color + assignee chips + Borrar / Dibujar / Enviar action row.
            // Rendered flush against the outer sheet's bottom edge (topStart/topEnd corners
            // are rounded inside the panel itself for the sheet-lip look).
            PostItControlPanel(
                members = members,
                selectedNoteColor = noteColor,
                onNoteColorSelect = { newColor ->
                    // Three-step propagation, in order of increasing durability:
                    //  1. Local composable state — sheet re-tints in the same frame.
                    //  2. Parent VM's `_postIts` — so a close/reopen cycle before the
                    //     60s sync tick picks up the new color from the source of truth
                    //     rather than reverting to yellow (was the bug).
                    //  3. Backend PUT — survives app restart / other devices' sync.
                    //     Fire-and-forget: network failure is non-fatal, the local pick
                    //     stays until the session ends.
                    val hex = newColor.toHex()
                    noteColor = newColor
                    onColorNotaChanged(hex)
                    persistenceScope.launch {
                        postItRepository.updateColorNota(state.id, hex)
                    }
                },
                selectedBrushByte = brushByte,
                onBrushSelect = { brushByte = it },
                selectedAssigneeId = assignedUserId,
                onAssigneeSelect = { assignedUserId = it },
                onBorrar = { pizarraViewRef?.clearCanvas() },
                // "Pintar" is currently just a visual affordance — brush selection is
                // already active in real time via [LaunchedEffect(brushByte)] above.
                // Reserved for future palette-mode toggles (e.g. eraser vs brush).
                onPintar = { /* no-op — brush is always active */ },
                // "Enviar" saves + closes. Any in-flight strokes flush via the existing
                // save() debounce; then we minimize back to the pizarra board.
                onEnviar = {
                    pizarraViewModel?.stop()
                    pizarraViewModel?._bitmapState?.value = null
                    onMinimize()
                },
            )
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

// ─────────────────────────────────────────────────────────────────────────────────────
// POST-IT CONTROL PANEL — bottom-sheet controls for the expanded drawing view.
// ─────────────────────────────────────────────────────────────────────────────────────

/**
 * Pastel palette for the "Color de la nota" (note background) selector.
 * These are display colors; the wire representation is the hex string via [toHex].
 */
val NOTE_PASTELS: List<Color> =
    listOf(
        Color(0xFFFFF9C4), // Yellow (default)
        Color(0xFFC8E6C9), // Green
        Color(0xFFBBDEFB), // Blue
        Color(0xFFE1BEE7), // Purple
        Color(0xFFF8BBD0), // Pink
    )

/**
 * Vibrant palette for the "Color del pincel" selector. Each entry pairs the display
 * color with the byte code that `PizarraView.createPaint` maps back to it — keep this
 * table in lock-step with the `createPaint` switch, otherwise strokes will render in
 * a different color than what the user selected in the picker.
 */
data class BrushSwatch(
    val byteCode: Byte,
    val color: Color,
)

val BRUSH_SWATCHES: List<BrushSwatch> =
    listOf(
        BrushSwatch(1.toByte(), Color(0xFFFBC02D)), // Yellow
        BrushSwatch(2.toByte(), Color(0xFF388E3C)), // Green
        BrushSwatch(3.toByte(), Color(0xFF1976D2)), // Blue
        BrushSwatch(4.toByte(), Color(0xFF673AB7)), // Purple
        BrushSwatch(5.toByte(), Color(0xFFE91E63)), // Fuchsia
        BrushSwatch(6.toByte(), Color.Black),
        BrushSwatch(7.toByte(), Color.White),
    )

fun Color.toHex(): String {
    val argb = toArgb()
    return "#%06X".format(0xFFFFFF and argb)
}

/**
 * Inverse of [Color.toHex] — parses a stored `colorNota` hex string back into a
 * Compose [Color]. Handles both `#RRGGBB` and `#AARRGGBB` variants, with graceful
 * fallback to `null` on unparseable input (blank strings, missing `#`, invalid chars).
 *
 * When the returned Color matches one of [NOTE_PASTELS] exactly (which it should,
 * since we only ever persist palette values), the `ColorSwatchDot`'s selected-check
 * (`pastel.toArgb() == selected.toArgb()`) lights up the correct dot.
 */
fun parseNoteColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val trimmed = hex.trim().removePrefix("#")
    return try {
        val argb =
            when (trimmed.length) {
                6 -> (0xFF000000.toInt()) or trimmed.toLong(16).toInt()
                8 -> trimmed.toLong(16).toInt()
                else -> return null
            }
        Color(argb)
    } catch (e: NumberFormatException) {
        null
    }
}

@Composable
private fun ColorSwatchDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    borderColor: Color = Color(0xFFBDBDBD),
) {
    Box(
        modifier =
            Modifier
                .size(if (selected) 32.dp else 28.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) Burgundy else borderColor,
                    shape = CircleShape,
                ).clickable(onClick = onClick),
    )
}

@Composable
fun NoteColorSelector(
    selected: Color,
    onSelect: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NOTE_PASTELS.forEach { pastel ->
            ColorSwatchDot(
                color = pastel,
                selected = pastel.toArgb() == selected.toArgb(),
                onClick = { onSelect(pastel) },
            )
        }
    }
}

@Composable
fun BrushColorSelector(
    selectedByte: Byte,
    onSelect: (Byte) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BRUSH_SWATCHES.forEach { swatch ->
            ColorSwatchDot(
                color = swatch.color,
                selected = swatch.byteCode == selectedByte,
                onClick = { onSelect(swatch.byteCode) },
                // Explicit gray border on the WHITE swatch so it doesn't disappear
                // into the card background.
                borderColor = if (swatch.color == Color.White) Color(0xFF757575) else Color(0xFFBDBDBD),
            )
        }
    }
}

@Composable
fun AssigneeChips(
    members: List<Usuario>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (members.isEmpty()) {
        Text(
            "Cargando miembros…",
            fontSize = 13.sp,
            color = TextoGris,
            modifier = modifier,
        )
        return
    }
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        members.forEach { member ->
            val isSelected = member.id == selectedId
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) LilaLight else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) LilaPrimary else TextoGris.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(50),
                        ).clickable {
                            // Toggle: tapping the selected chip clears the assignment.
                            onSelect(if (isSelected) null else member.id)
                        }.padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "⊕ ${member.nombre}",
                    fontSize = 13.sp,
                    color = if (isSelected) LilaPrimary else TextoGris,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun PostItActionRow(
    onBorrar: () -> Unit,
    onPintar: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Default OutlinedButton contentPadding is 24dp horizontal — with three equal-weight
    // buttons on a 324dp-wide row that leaves ~5dp per button for icon + text, forcing the
    // labels to ellipsize ("Bo…", "Di…", "En…"). Override to a compact 6dp padding and
    // shrink the icon/text so all three fit comfortably with a hair of breathing room.
    val compactPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onBorrar,
            modifier = Modifier.weight(1f).height(42.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoGris),
            border = BorderStroke(1.dp, TextoGris.copy(alpha = 0.5f)),
            contentPadding = compactPadding,
        ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Borrar", fontSize = 12.sp, maxLines = 1)
        }
        OutlinedButton(
            onClick = onPintar,
            modifier = Modifier.weight(1f).height(42.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = LilaPrimary),
            border = BorderStroke(1.dp, LilaPrimary),
            contentPadding = compactPadding,
        ) {
            Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Pintar", fontSize = 12.sp, maxLines = 1)
        }
        OutlinedButton(
            onClick = onEnviar,
            modifier = Modifier.weight(1f).height(42.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Burgundy),
            border = BorderStroke(1.dp, Burgundy),
            contentPadding = compactPadding,
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Enviar", fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun PostItControlPanel(
    members: List<Usuario>,
    selectedNoteColor: Color,
    onNoteColorSelect: (Color) -> Unit,
    selectedBrushByte: Byte,
    onBrushSelect: (Byte) -> Unit,
    selectedAssigneeId: Long?,
    onAssigneeSelect: (Long?) -> Unit,
    onBorrar: () -> Unit,
    onPintar: () -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // `navigationBarsPadding()` pushes the last row up above the Android system nav bar
    // (gesture-handle or 3-button) so the "Enviar" button never sits under the OS chrome.
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 14.dp)
                .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Color de la nota:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        NoteColorSelector(selected = selectedNoteColor, onSelect = onNoteColorSelect)

        Text("Color del pincel:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        BrushColorSelector(selectedByte = selectedBrushByte, onSelect = onBrushSelect)

        Text("Enviar nota a:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        AssigneeChips(members = members, selectedId = selectedAssigneeId, onSelect = onAssigneeSelect)

        Spacer(Modifier.height(4.dp))
        PostItActionRow(onBorrar = onBorrar, onPintar = onPintar, onEnviar = onEnviar)
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
