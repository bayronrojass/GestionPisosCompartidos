package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
fun PrincipalInicioSesin(modifier: Modifier = Modifier) {
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
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 0.dp,
                        y = 80.dp,
                    ).requiredWidth(width = 390.dp)
                    .requiredHeight(height = 885.dp)
                    .clip(shape = RoundedCornerShape(40.dp))
                    .background(color = Color(0xfff8f8f8)),
        )
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = 0.dp,
                        y = 100.dp,
                    ).requiredWidth(width = 50.dp)
                    .requiredHeight(height = 10.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(color = Color(0xff6c6c6c)),
        )
        Image(
            painter = painterResource(id = R.drawable.ilustracioniniciosesion),
            contentDescription = "ilustracion inicio sesin",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 142.dp,
                        y = 153.dp,
                    ).requiredWidth(width = 108.dp)
                    .requiredHeight(height = 153.dp),
        )
        Text(
            text = "¡Hola, de nuevo!",
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 1.33.em,
            style = MaterialTheme.typography.displaySmall,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = 1.dp,
                        y = 342.dp,
                    ).requiredWidth(width = 350.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 430.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(15.dp),
                    ).padding(
                        start = 20.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 9.dp,
                    ),
        ) {
            Text(
                text = "Nombre Usuario",
                color = Color(0xff6c6c6c),
                textAlign = TextAlign.Center,
                style =
                    TextStyle(
                        fontSize = 16.sp,
                    ),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 500.dp,
                    ).requiredWidth(width = 350.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(15.dp),
                    ).padding(
                        start = 19.dp,
                        end = 20.dp,
                        top = 10.dp,
                        bottom = 10.dp,
                    ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(199.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 30.dp),
            ) {
                Text(
                    text = "Contraseña",
                    color = Color(0xff6c6c6c),
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                        ),
                )
                Image(
                    painter = painterResource(id = R.drawable.frameojo),
                    contentDescription = "Frame",
                    colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                    modifier =
                        Modifier
                            .requiredSize(size = 24.dp),
                )
            }
        }
        Text(
            text = "¿Has olvidado tu contraseña?",
            color = Color(0xff581327),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = 1.dp,
                        y = 580.dp,
                    ).requiredWidth(width = 212.dp)
                    .requiredHeight(height = 20.dp),
        )
        Text(
            text = "o",
            color = Color(0xff6c6c6c),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = 0.dp,
                        y = 620.dp,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.line3),
            contentDescription = "Line 3",
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 210.dp,
                        y = 630.5.dp,
                    ).requiredWidth(width = 160.dp)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c))),
        )
        Image(
            painter = painterResource(id = R.drawable.line4),
            contentDescription = "Line 4",
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 630.5.dp,
                    ).requiredWidth(width = 160.dp)
                    .border(border = BorderStroke(1.dp, Color(0xff6c6c6c))),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 245.dp,
                        y = 670.dp,
                    ).requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(15.dp),
                    ),
        ) {
            Image(
                painter = painterResource(id = R.drawable.facebook),
                contentDescription = "google",
                modifier =
                    Modifier
                        .requiredSize(size = 28.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 165.dp,
                        y = 670.dp,
                    ).requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(15.dp),
                    ),
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "google",
                modifier =
                    Modifier
                        .requiredSize(size = 24.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 85.dp,
                        y = 670.dp,
                    ).requiredWidth(width = 60.dp)
                    .requiredHeight(height = 50.dp)
                    .clip(shape = RoundedCornerShape(15.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(15.dp),
                    ).padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 12.dp,
                    ),
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple),
                contentDescription = "apple",
                colorFilter = ColorFilter.tint(Color.Black),
                modifier =
                    Modifier
                        .requiredSize(size = 30.dp),
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
                        x = 1.5.dp,
                        y = 761.dp,
                    ).requiredWidth(width = 273.dp)
                    .requiredHeight(height = 19.dp),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun PrincipalInicioSesinPreview() {
    PrincipalInicioSesin(Modifier)
}
