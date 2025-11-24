package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.compose.AndroidFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizarraScreen(casaId: Long) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .weight(1f),
        ) {
            AndroidFragment<Pizarra>(
                modifier = Modifier.fillMaxSize(),
                arguments = Bundle().apply { putLong("casa_id", casaId) },
            )
        }
    }
}

// Función de preview con argumento de ejemplo
@Preview
@Composable
fun PizarraPreview() {
    PizarraScreen(casaId = 123L)
}
