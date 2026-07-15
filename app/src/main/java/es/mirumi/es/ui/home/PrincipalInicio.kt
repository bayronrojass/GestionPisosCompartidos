package es.mirumi.es.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import es.mirumi.es.R
import es.mirumi.es.ui.navigation.Route
import es.mirumi.es.ui.theme.Burgundy

@Composable
fun PrincipalInicio(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 27.dp,
                        y = 4.dp,
                    ).requiredWidth(width = 343.dp)
                    .requiredHeight(height = 40.dp),
        ) {
        }
        Text(
            text = "MiRumi",
            color = Color.Black,
            lineHeight = 0.71.em,
            style =
                TextStyle(
                    fontSize = 55.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = (-85).dp,
                        y = 105.dp,
                    ).requiredWidth(width = 190.dp)
                    .requiredHeight(height = 60.dp),
        )
        Text(
            text = "Tu compañero virtual para una convivencia sin caos",
            color = Color.Black,
            lineHeight = 1.25.em,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 170.dp,
                    ).requiredWidth(width = 271.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.ilustracionprincipalinicio),
            contentDescription = "Ilustracin principal inicio",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 30.dp,
                        y = 249.dp,
                    ).requiredWidth(width = 330.dp)
                    .requiredHeight(height = 315.dp),
        )
        Column(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 630.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { navController.navigate(Route.InicioSesion.route) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(25.dp),
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            textAlign = TextAlign.Center,
            text =
                buildAnnotatedString {
                    withStyle(
                        style =
                            SpanStyle(
                                color = Color(0xff6c6c6c),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    ) { append("¿No tienes cuenta todavía?") }
                    withStyle(
                        style =
                            SpanStyle(
                                color = Burgundy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    ) { append(" ") }
                    withStyle(
                        style =
                            SpanStyle(
                                color = Burgundy,
                                fontSize = 15.sp,
                            ),
                    ) { append("Regístrate") }
                },
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = (-0.5).dp,
                        y = 720.dp,
                    ).clickable { navController.navigate(Route.Registro.route) },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PrincipalInicioPreview() {
    PrincipalInicio(rememberNavController(), Modifier)
}
