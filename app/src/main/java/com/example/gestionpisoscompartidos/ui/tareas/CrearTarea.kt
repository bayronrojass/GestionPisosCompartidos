package com.example.gestionpisoscompartidos.ui.tareas

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.R

@Composable
fun AadirTarea(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredWidth(width = 350.dp)
                .requiredHeight(height = 404.dp)
                .clip(shape = RoundedCornerShape(20.dp))
                .background(color = Color.White),
    ) {
        Image(
            painter = painterResource(id = R.drawable.iconocerrar),
            contentDescription = "Icono Cerrar",
            colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 306.dp,
                        y = 20.dp,
                    ).requiredSize(size = 24.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 30.dp,
                    ),
        ) {
            Text(
                text = "Nombre tarea",
                color = Color(0xff6c6c6c),
                style = MaterialTheme.typography.headlineSmall,
                modifier =
                    Modifier
                        .requiredWidth(width = 159.dp)
                        .requiredHeight(height = 25.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
            )
            Image(
                painter = painterResource(id = R.drawable.iconolapiz),
                contentDescription = "Frame",
                colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                modifier =
                    Modifier
                        .requiredSize(size = 24.dp),
            )
        }
        Text(
            text = "Fecha tarea:",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 75.dp,
                    ).requiredWidth(width = 110.dp)
                    .requiredHeight(height = 20.dp),
        )
        InputChip(
            label = {
                Text(
                    text = "18 de noviembre",
                    color = Color(0xff6c6c6c),
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                        ),
                )
            },
            avatar = {
                Image(
                    painter = painterResource(id = R.drawable.iconocalendario),
                    contentDescription = "Frame",
                    colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
                    modifier =
                        Modifier
                            .requiredSize(size = 24.dp),
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors =
                FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xfffbfafa),
                ),
            selected = true,
            onClick = { },
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 115.dp,
                    ),
        )
        Text(
            text = "Prioridad de la tarea:",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 165.dp,
                    ).requiredWidth(width = 139.dp)
                    .requiredHeight(height = 20.dp),
        )
        Property1baja(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 178.dp,
                        y = 205.dp,
                    ),
        )
        Property1media(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 99.dp,
                        y = 205.dp,
                    ),
        )
        Property1alta(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 205.dp,
                    ),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 20.dp,
                        y = 243.dp,
                    ).requiredWidth(width = 310.dp),
        ) {
            Text(
                text = "Compartir gasto con:",
                color = Color.Black,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 20.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 19.dp),
            ) {
                AadirUsuario()
                AadirUsuario()
                AadirUsuario()
                AadirUsuario()
            }
        }
        Property1cerrado(
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 290.dp,
                        y = 325.dp,
                    ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(
                        x = 90.dp,
                        y = 334.dp,
                    ).requiredWidth(width = 170.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                        shape = RoundedCornerShape(20.dp),
                    ).padding(
                        horizontal = 55.dp,
                        vertical = 10.dp,
                    ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Aceptar",
                    color = Color(0xff6c6c6c),
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
        }
    }
}

@Composable
fun Property1baja(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .requiredWidth(width = 70.dp)
                .requiredHeight(height = 20.dp)
                .clip(shape = RoundedCornerShape(17.dp))
                .background(color = Color(0xffa9e6a8))
                .padding(
                    horizontal = 10.dp,
                    vertical = 2.dp,
                ),
    ) {
        Text(
            text = "Baja",
            color = Color(0xff2d5c2c),
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
fun AadirUsuario(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.Start),
        verticalAlignment = Alignment.Bottom,
        modifier =
            modifier
                .requiredHeight(height = 22.dp)
                .clip(shape = RoundedCornerShape(25.dp))
                .background(color = Color(0xfffbfafa))
                .border(
                    border = BorderStroke(1.dp, Color(0xff6c6c6c)),
                    shape = RoundedCornerShape(25.dp),
                ).padding(
                    start = 3.dp,
                    end = 6.dp,
                    top = 1.dp,
                    bottom = 3.dp,
                ),
    ) {
        Image(
            painter = painterResource(id = R.drawable.compartirnotaiconos),
            contentDescription = "compartir nota iconos",
            colorFilter = ColorFilter.tint(Color(0xff6c6c6c)),
            modifier =
                Modifier
                    .requiredSize(size = 16.dp),
        )
        Text(
            text = "Marta",
            color = Color(0xff6c6c6c),
            style =
                TextStyle(
                    fontSize = 14.sp,
                ),
            modifier =
                Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically),
        )
    }
}

@Preview(widthDp = 350, heightDp = 404)
@Composable
private fun AadirTareaPreview() {
    AadirTarea(Modifier)
}
