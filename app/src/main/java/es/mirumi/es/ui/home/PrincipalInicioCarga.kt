package es.mirumi.es.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.mirumi.es.R
import es.mirumi.es.ui.navigation.Route
import kotlinx.coroutines.delay

@Composable
fun PrincipalInicioCarga(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate(Route.InicioPrincipal.route) {
            // Elimina esta pantalla de la pila para que no se pueda volver atrás
            popUpTo(Route.InicioCarga.route) { inclusive = true }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    brush =
                        Brush.linearGradient(
                            0f to Color(0xffff81a5),
                            1f to Color(0xffcf0a42),
                            start = Offset(195f, 0f),
                            end = Offset(195f, 844f),
                        ),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 27.dp,
                        y = 4.dp,
                    )
                    .requiredWidth(width = 343.dp)
                    .requiredHeight(height = 40.dp),
        ) {
        }
        Image(
            painter = painterResource(id = R.drawable.iconoapp),
            contentDescription = "icono app",
            colorFilter = ColorFilter.tint(Color.White),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 143.43.dp,
                        y = 340.dp,
                    )
                    .requiredWidth(width = 104.dp)
                    .requiredHeight(height = 93.dp),
        )
        Text(
            text = "MiRumi",
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 0.64.em,
            style =
                TextStyle(
                    fontSize = 30.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = 0.dp,
                        y = 453.dp,
                    )
                    .requiredWidth(width = 130.dp)
                    .requiredHeight(height = 40.dp),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PrincipalInicioCargaPreview() {
    PrincipalInicioCarga(rememberNavController(), Modifier)
}
