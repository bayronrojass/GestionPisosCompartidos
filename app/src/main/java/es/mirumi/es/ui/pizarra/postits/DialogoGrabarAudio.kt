package es.mirumi.es.ui.pizarra.postits

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.IOException

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

    // Función para iniciar la grabación
    fun startRecording() {
        val archivoTemp = File(context.cacheDir, "nota_voz_${System.currentTimeMillis()}.mp3")
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

    // Función para detener la grabación
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
                // Aquí usamos la forma segura de comprobar el permiso
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
