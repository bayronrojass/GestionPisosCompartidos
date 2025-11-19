package com.example.gestionpisoscompartidos.ui.codigoQR

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CodigoQRScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Código QR",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Aquí puedes agregar más componentes Compose
        // Por ejemplo, un lector de QR:
        // QRCodeReader { resultado ->
        //     viewModel.procesarCodigoQR(resultado)
        // }
    }
}
