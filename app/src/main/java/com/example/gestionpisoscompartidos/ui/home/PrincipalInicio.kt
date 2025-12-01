package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.gestionpisoscompartidos.R

@Composable
fun PrincipalInicio(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredWidth(width = 390.dp)
                .requiredHeight(height = 844.dp)
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
                        y = 109.dp,
                    ).requiredWidth(width = 190.dp)
                    .requiredHeight(height = 50.dp),
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 630.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.Black)
                    .border(
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(15.dp),
                    ).padding(all = 10.dp),
        ) {
            Text(
                text = "Iniciar sesión",
                color = Color.White,
                textAlign = TextAlign.Center,
                style =
                    TextStyle(
                        fontSize = 18.sp,
                    ),
            )
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
                                color = Color(0xff581327),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    ) { append(" ") }
                    withStyle(
                        style =
                            SpanStyle(
                                color = Color(0xff581327),
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
                    ),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PrincipalInicioPreview() {
    PrincipalInicio(Modifier)
}
