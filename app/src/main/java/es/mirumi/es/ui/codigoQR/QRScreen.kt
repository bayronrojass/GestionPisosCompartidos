package es.mirumi.es.ui.codigoQR

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QRScreen(
    casaIdActual: Long,
    viewModel: CodigoQRViewModel,
) {
    val context = LocalContext.current
    val mensajeQR by viewModel.mensajeQR.collectAsState()

    LaunchedEffect(mensajeQR) {
        mensajeQR?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    val qrScannerLauncher =
        rememberLauncherForActivityResult(
            contract = ScanContract(),
            onResult = { result ->
                if (result.contents != null) {
                    try {
                        val uri = android.net.Uri.parse(result.contents)
                        val casaIdString = uri.getQueryParameter("casaId")

                        val casaIdL = casaIdString?.toLongOrNull()

                        if (casaIdL != null) {
                            viewModel.procesarQRUnirseCasa(casaIdL)
                        } else {
                            Toast.makeText(context, "El QR no es de Mirumi", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Formato de QR incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Invitar a esta casa",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(16.dp))

        val contenidoQR = "mirumi://invite?casaId=$casaIdActual"

        val qrBitmap: android.graphics.Bitmap? =
            remember(contenidoQR) {
                es.mirumi.es.ui.codigoQR.CodigoQR
                    .generateQRCodeBitmap(contenidoQR)
            }

        if (qrBitmap != null) {
            Box(
                modifier =
                    Modifier
                        .background(Color.White)
                        .padding(12.dp),
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Código QR",
                    modifier = Modifier.size(250.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "¿Quieres unirte a otro piso?",
            fontSize = 18.sp,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val options =
                ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Enfoca el QR del anfitrión")
                    setCameraId(0)
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                }
            qrScannerLauncher.launch(options)
        }) {
            Text("Escanear QR", color = Color.White)
        }
    }
}
