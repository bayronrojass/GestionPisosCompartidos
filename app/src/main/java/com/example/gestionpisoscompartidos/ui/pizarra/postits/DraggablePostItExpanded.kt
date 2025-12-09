package com.example.gestionpisoscompartidos.ui.pizarra.postits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraView
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModel
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun ExpandedPostIt(
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    state: PostItState,
) {
    val pizarraViewModel: PizarraViewModel =
        viewModel(
            factory = PizarraViewModelFactory(state.lienzoId),
        )

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
        Row {
            InputChip(
                label = { Text("Cerrar") },
                avatar = { Icon(Icons.Default.Close, "Cerrar") },
                selected = true,
                onClick = {
                    pizarraViewModel.stop()
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
                    pizarraViewModel.stop()
                    onMinimize()
                },
                colors = chipColors,
                border = null,
            )
        }
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
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current

            val pizarraView =
                remember(state.lienzoId) {
                    PizarraView(context).apply {
                        this.activatedDraw = true
                        setModel(pizarraViewModel)
                    }
                }

            LaunchedEffect(pizarraView, lifecycleOwner) {
                lifecycleOwner.lifecycleScope.launch {
                    pizarraViewModel.load()

                    pizarraViewModel.bitmapState.collect { bitmap ->
                        bitmap?.let {
                            pizarraView.setBackgroundBitmap(it)
                        }
                    }
                }
            }

            AndroidView(
                factory = { pizarraView },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
