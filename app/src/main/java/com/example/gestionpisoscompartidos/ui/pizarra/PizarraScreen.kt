package com.example.gestionpisoscompartidos.ui.pizarra

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.compose.AndroidFragment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizarraScreen(
    casaId: Long,
) {
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
