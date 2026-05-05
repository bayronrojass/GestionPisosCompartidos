package es.mirumi.es.ui.pizarra.postits

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import es.mirumi.es.BuildConfig
import es.mirumi.es.R
import es.mirumi.es.ui.pizarra.PizarraView
import es.mirumi.es.ui.pizarra.PizarraViewModel
import es.mirumi.es.ui.pizarra.PizarraViewModelFactory
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun ExpandedPostIt(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    state: PostItState,
) {
    // Si es un audio, no necesitamos instanciar el ViewModel de la Pizarra porque no hay lienzo
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

        // 🔴 AQUI DECIDIMOS QUÉ DIBUJAR: ¿AUDIO O PIZARRA? 🔴
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
            // Es un DIBUJO normal
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
                            viewModel.lienzoId = state.lienzoId
                            viewModel.lastLoaded = Instant.ofEpochMilli(1000000)
                            view.load()
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

// 🟢 ESTO VA FUERA, ES UN COMPONENTE INDEPENDIENTE 🟢
@Composable
fun AudioPostIt(urlAudio: String) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Usamos BuildConfig.BASE_URL para que se conecte a tu Spring Boot
    val fullUrl = "${BuildConfig.BASE_URL}$urlAudio"

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF59D)), // Color amarillito post-it
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
                        mediaPlayer =
                            MediaPlayer().apply {
                                setDataSource(fullUrl)
                                prepareAsync()
                                setOnPreparedListener {
                                    start()
                                    isPlaying = true
                                }
                                setOnCompletionListener {
                                    isPlaying = false
                                    release()
                                    mediaPlayer = null
                                }
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
