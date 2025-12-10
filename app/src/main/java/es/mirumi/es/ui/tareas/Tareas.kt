package es.mirumi.es.ui.tareas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.mirumi.es.R

@Composable
fun Tareas(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 27.dp,
                        y = 4.dp,
                    )
                    .fillMaxSize(),
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
                    )
                    .requiredWidth(width = 260.dp)
                    .requiredHeight(height = 24.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopCenter)
                        .offset(
                            x = 0.dp,
                            y = 0.dp,
                        )
                        .requiredWidth(width = 260.dp)
                        .requiredHeight(height = 24.dp)
                        .clip(shape = RoundedCornerShape(26.dp))
                        .background(color = Color.White)
                        .border(
                            border = BorderStroke(3.dp, Color.White),
                            shape = RoundedCornerShape(26.dp),
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(26.dp),
                        ),
            )
            Box(
                modifier =
                    Modifier
                        .align(alignment = Alignment.TopCenter)
                        .offset(
                            x = (-65).dp,
                            y = 0.dp,
                        )
                        .requiredWidth(width = 130.dp)
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
                        )
                        .requiredWidth(width = 86.dp)
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
                        )
                        .requiredWidth(width = 102.dp)
                        .requiredHeight(height = 20.dp),
            )
        }
        Text(
            text = "ASIGNACIÓN MENSUAL",
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
                        y = 210.dp,
                    )
                    .requiredWidth(width = 210.dp)
                    .requiredHeight(height = 20.dp),
        )
        AsignacinMensual(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 250.dp,
                    ),
        )
        Text(
            text = "MIS TAREAS",
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
                        y = 360.dp,
                    )
                    .requiredWidth(width = 198.dp)
                    .requiredHeight(height = 20.dp),
        )
        Property1tuya(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 410.dp,
                    ),
        )
        Property1cerrado(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 310.dp,
                        y = 561.dp,
                    ),
        )
        Property1TuyaSinHora(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 570.dp,
                    ),
        )
        Text(
            text = "OTRAS",
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
                        y = 762.dp,
                    )
                    .requiredWidth(width = 198.dp)
                    .requiredHeight(height = 23.dp),
        )
        Property1otras(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 820.dp,
                    ),
        )
        Property1otras(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 980.dp,
                    ),
        )
    }
}

@Composable
fun AsignacinMensual(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .requiredHeight(height = 60.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(all = 10.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .requiredWidth(width = 110.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(7.5.dp))
                    .background(color = Color.Black)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp,
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(7.5.dp),
                    ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .requiredHeight(height = 25.dp)
                        .padding(vertical = 2.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconorotar),
                    contentDescription = "icono",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier =
                        Modifier
                            .requiredSize(size = 21.dp),
                )
                Text(
                    text = "Rotación",
                    color = Color.White,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                        ),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .requiredWidth(width = 110.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(7.5.dp))
                    .background(color = Color.White)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp,
                    ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .requiredHeight(height = 25.dp)
                        .padding(vertical = 2.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconoaleatorio),
                    contentDescription = "icono",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier =
                        Modifier
                            .requiredSize(size = 20.dp),
                )
                Text(
                    text = "Aleatorio",
                    color = Color.Black,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                        ),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .requiredWidth(width = 110.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(7.5.dp))
                    .background(color = Color.White)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 7.dp,
                    ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .requiredHeight(height = 25.dp)
                        .padding(vertical = 2.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconomanual),
                    contentDescription = "icono",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier =
                        Modifier
                            .requiredSize(size = 24.dp)
                            .rotate(degrees = -180f),
                )
                Text(
                    text = "Manual",
                    color = Color.Black,
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
fun Property1tuya(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(all = 20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
            modifier =
                Modifier
                    .requiredWidth(width = 153.dp),
        ) {
            Text(
                text = "Limpiar cocina ",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Property1alta()
                Property1Default()
            }
        }
        TextField(
            value = "",
            onValueChange = {},
            label = {
                Text(
                    text = "Completar",
                    color = Color(0xff6c6c6c),
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
            },
            placeholder = { Text("14 Nov") },
            textStyle =
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .requiredWidth(width = 310.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(width = 141.dp),
        ) {
            Text(
                text = "Solicitar cambio",
                color = Color(0xff6c6c6c),
                textDecoration = TextDecoration.Underline,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .requiredWidth(width = 117.dp)
                        .requiredHeight(height = 20.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.iconocambio),
                contentDescription = "icono cambio",
                colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                modifier =
                    Modifier
                        .requiredSize(size = 24.dp),
            )
        }
    }
}

@Composable
fun Property1alta(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 70.dp)
                .requiredHeight(height = 20.dp)
                .clip(shape = RoundedCornerShape(17.dp))
                .background(color = Color(0xffff6490))
                .padding(
                    horizontal = 10.dp,
                    vertical = 2.dp,
                ),
    ) {
        Text(
            text = "Alta",
            color = Color(0xff581327),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

@Composable
fun Property1Default(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredHeight(height = 20.dp)
                .clip(shape = RoundedCornerShape(25.dp))
                .background(color = Color(0xffff6490))
                .padding(
                    horizontal = 15.dp,
                    vertical = 10.dp,
                ),
    ) {
        Text(
            text = "de 16 a 17h",
            color = Color(0xff5a1428),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

@Composable
fun Property1cerrado(modifier: Modifier = Modifier) {
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
                    )
                    .requiredWidth(width = 60.dp)
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
fun Property1TuyaSinHora(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(all = 20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
            modifier =
                Modifier
                    .requiredWidth(width = 153.dp),
        ) {
            Text(
                text = "Arreglar grifo ",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Property1media()
                Image(
                    painter = painterResource(id = R.drawable.vectormas),
                    contentDescription = "Vector",
                    modifier =
                        Modifier
                            .requiredSize(size = 16.dp),
                )
            }
        }
        TextField(
            value = "",
            onValueChange = {},
            label = {
                Text(
                    text = "Completar",
                    color = Color(0xff6c6c6c),
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
            },
            placeholder = { Text("14 Nov") },
            textStyle =
                TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .requiredWidth(width = 310.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(width = 141.dp),
        ) {
            Text(
                text = "Solicitar cambio",
                color = Color(0xff6c6c6c),
                textDecoration = TextDecoration.Underline,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .requiredWidth(width = 117.dp)
                        .requiredHeight(height = 20.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.iconocambio),
                contentDescription = "icono cambio",
                colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                modifier =
                    Modifier
                        .requiredSize(size = 24.dp),
            )
        }
    }
}

@Composable
fun Property1media(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 70.dp)
                .requiredHeight(height = 20.dp)
                .clip(shape = RoundedCornerShape(17.dp))
                .background(color = Color(0xffddc1fb))
                .padding(
                    horizontal = 10.dp,
                    vertical = 2.dp,
                ),
    ) {
        Text(
            text = "Media",
            color = Color(0xff5d427a),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

@Composable
fun Property1default(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 60.dp)
                .requiredHeight(height = 40.dp)
                .clip(shape = RoundedCornerShape(30.dp))
                .background(color = Color.Black)
                .padding(
                    horizontal = 17.dp,
                    vertical = 14.dp,
                ),
    ) {
        Image(
            painter = painterResource(id = R.drawable.iconocuentas),
            contentDescription = "Property 1=tarjetaa",
            colorFilter = ColorFilter.tint(Color.White),
            modifier =
                Modifier
                    .requiredWidth(width = 26.dp)
                    .requiredHeight(height = 18.dp),
        )
    }
}

@Composable
fun Property1otras(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .clip(shape = RoundedCornerShape(15.dp))
                .background(color = Color.White)
                .padding(all = 20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(15.dp),
                ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
            modifier =
                Modifier
                    .requiredWidth(width = 200.dp),
        ) {
            Text(
                text = "Tirar basura",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            )
            Text(
                text = "Asignado a Daniel",
                color = Color(0xff6c6c6c),
                style =
                    TextStyle(
                        fontSize = 16.sp,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 20.dp),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(77.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .requiredWidth(width = 310.dp),
        ) {
            Text(
                text = "14 Nov",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .requiredWidth(width = 53.dp)
                        .requiredHeight(height = 20.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Property1media()
                Property1Variant2()
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recordar",
                color = Color(0xff6c6c6c),
                textDecoration = TextDecoration.Underline,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .requiredWidth(width = 70.dp)
                        .requiredHeight(height = 20.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.frame),
                contentDescription = "Frame",
                colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                modifier =
                    Modifier
                        .requiredSize(size = 24.dp),
            )
        }
    }
}

@Composable
fun Property1Variant2(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredHeight(height = 20.dp)
                .clip(shape = RoundedCornerShape(25.dp))
                .background(color = Color(0xffddc1fb))
                .padding(
                    horizontal = 15.dp,
                    vertical = 10.dp,
                ),
    ) {
        Text(
            text = "de 16 a 17h",
            color = Color(0xff5d427a),
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

@Preview(widthDp = 390, heightDp = 1248)
@Composable
private fun TareasPreview() {
    Tareas(Modifier)
}
