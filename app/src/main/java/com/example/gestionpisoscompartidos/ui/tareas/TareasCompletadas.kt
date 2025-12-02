package com.example.gestionpisoscompartidos.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.rotate
import com.example.gestionpisoscompartidos.R

@Composable
fun TareasCompletadas(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                // .requiredHeight(height = 1165.dp)
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 27.dp,
                        y = 4.dp,
                    ).fillMaxSize(),
            // .requiredHeight(height = 40.dp),
        ) {
        }
        Text(
            text = "Tareas",
            color = Color.Black,
            style = MaterialTheme.typography.displaySmall,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 63.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 65.dp,
                        y = 143.dp,
                    ).requiredWidth(width = 260.dp)
                    .requiredHeight(height = 24.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopCenter)
                        .offset(
                            x = 0.dp,
                            y = 0.dp,
                        ).requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                        .clip(shape = RoundedCornerShape(26.dp))
                        .background(color = Color.White)
                        .border(
                            border = BorderStroke(3.dp, Color.White),
                            shape = RoundedCornerShape(26.dp),
                        ).shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                        ),
            )
            Box(
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopCenter)
                        .offset(
                            x = 65.dp,
                            y = 0.dp,
                        ).requiredWidth(width = 130.dp)
                        .requiredHeight(height = 24.dp)
                        .clip(shape = RoundedCornerShape(26.dp))
                        .background(color = Color(0xffddc1fb)),
            )
            Text(
                text = "Pendientes",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 22.dp,
                            y = 2.09.dp,
                        ).requiredWidth(width = 86.dp)
                        .requiredHeight(height = 20.dp),
            )
            Text(
                text = "Completadas",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopStart)
                        .offset(
                            x = 144.dp,
                            y = 2.09.dp,
                        ).requiredWidth(width = 102.dp)
                        .requiredHeight(height = 20.dp),
            )
        }
        Text(
            text = "SEMANALES",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 200.dp,
                    ).requiredWidth(width = 198.dp)
                    .requiredHeight(height = 20.dp),
        )
        TareaHecha(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 250.dp,
                    ),
        )
        TareaHecha(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 390.dp,
                    ),
        )
        TareaHecha(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 530.dp,
                    ),
        )
        Property2cerrado(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 310.dp,
                        y = 561.dp,
                    ),
        )
        Text(
            text = "MENSUALES",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 700.dp,
                    ).requiredWidth(width = 198.dp)
                    .requiredHeight(height = 20.dp),
        )
        Property2Default(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 740.dp,
                    ),
        )
        Property2Default(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 820.dp,
                    ),
        )
        Property2Default(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 920.dp,
                    ),
        )
        Text(
            text = "ver más",
            color = Color(0xff6c6c6c),
            textDecoration = TextDecoration.Underline,
            style =
                TextStyle(
                    fontSize = 14.sp,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopCenter)
                    .offset(
                        x = (-0.5).dp,
                        y = 1020.dp,
                    ),
        )
    }
}

@Composable
fun TareaHecha(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.End,
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .requiredHeight(height = 120.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 17.dp,
                    bottom = 20.dp,
                ).shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Text(
            text = "Dar de comer a Salchichón",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .requiredHeight(height = 24.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
            modifier =
                Modifier
                    .requiredWidth(width = 310.dp),
        ) {
            HechoPor()
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.frame),
                    contentDescription = "Frame",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier =
                        Modifier
                            .requiredSize(size = 24.dp)
                            .clip(shape = RoundedCornerShape(20.dp)),
                )
                Text(
                    text = "09 Nov",
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                        ),
                )
            }
        }
    }
}

@Composable
fun HechoPor(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 200.dp)
                .requiredHeight(height = 38.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(color = Color.White)
                .border(
                    border = BorderStroke(1.dp, Color(0xff939393)),
                    shape = RoundedCornerShape(20.dp),
                ).padding(
                    start = 2.dp,
                    end = 14.dp,
                    top = 2.dp,
                    bottom = 2.dp,
                ),
    ) {
        Property1Natalia()
        Text(
            text = "Hecho por Natalia",
            color = Color(0xff6c6c6c),
            textAlign = TextAlign.End,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .requiredWidth(width = 137.dp),
        )
    }
}

@Composable
fun Property1Dani(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredSize(size = 34.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = "foto",
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape),
        )
    }
}

@Composable
fun Property1Raquel(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredSize(size = 34.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = "foto",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape)
                    .border(
                        border = BorderStroke(0.dp, Color(0xfff8f8f8)),
                        shape = CircleShape,
                    ),
        )
    }
}

@Composable
fun Property1Natalia(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredSize(size = 34.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = "foto",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape)
                    .border(
                        border = BorderStroke(0.dp, Color(0xfff8f8f8)),
                        shape = CircleShape,
                    ),
        )
    }
}

@Composable
fun Property2cerrado(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredWidth(width = 60.dp)
                .requiredHeight(height = 170.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 0.dp,
                        y = 110.dp,
                    ).requiredWidth(width = 60.dp)
                    .requiredHeight(height = 60.dp)
                    .clip(shape = RoundedCornerShape(30.dp))
                    .rotate(degrees = 180f)
                    .background(color = Color.Black)
                    .padding(
                        horizontal = 15.dp,
                        vertical = 13.dp,
                    ),
        ) {
            Image(
                painter = painterResource(id = R.drawable.frame125),
                contentDescription = "Frame 125",
                modifier =
                    Modifier
                        .requiredWidth(width = 50.dp)
                        .requiredHeight(height = 50.dp)
                        .clip(shape = RoundedCornerShape(30.dp))
                        .rotate(degrees = 360f)
                        .padding(
                            horizontal = 15.dp,
                            vertical = 13.dp,
                        ),
            )
        }
    }
}

@Composable
fun Property2Default(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(
                    start = 20.dp,
                    end = 10.dp,
                    top = 18.dp,
                    bottom = 18.dp,
                ).shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(width = 300.dp),
        ) {
            Text(
                text = "Llamar cerrajero",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .requiredWidth(width = 230.dp),
            )
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .requiredWidth(width = 81.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.frame),
                    contentDescription = "Frame",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier =
                        Modifier
                            .requiredSize(size = 24.dp)
                            .clip(shape = RoundedCornerShape(20.dp)),
                )
                Text(
                    text = "5 Oct",
                    color = Color.Black,
                    textAlign = TextAlign.End,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                        ),
                    modifier =
                        Modifier
                            .requiredWidth(width = 59.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 1165)
@Composable
private fun TareasCompletadasPreview() {
    TareasCompletadas(Modifier)
}
