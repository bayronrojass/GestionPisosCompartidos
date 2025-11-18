package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.R
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    val scrollState = rememberScrollState()

    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()
    val eventosDelDia by viewModel.eventosDelDia.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxSize() // Ocupa todo el espacio disponible
                .verticalScroll(scrollState)
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(260.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .offset(x = 27.dp, y = 4.dp)
                        .requiredWidth(width = 343.dp),
            ) {
                // (Imágenes de perfil comentadas en tu código original)
            }

            Text(
                text = "Hola, \nUsuario!",
                color = Color.Black,
                lineHeight = 1.em,
                style = TextStyle(fontSize = 48.sp),
                modifier = Modifier.offset(x = 31.dp, y = 91.dp),
            )

            Image(
                painter = painterResource(id = R.drawable.home_union),
                contentDescription = "Union",
                modifier = Modifier.offset(x = 258.dp, y = 97.dp).requiredWidth(92.dp),
            )
            Image(
                painter = painterResource(id = R.drawable.home_vector33),
                contentDescription = "Vector 33",
                modifier =
                    Modifier
                        .offset(
                            x = 223.dp,
                            y = 102.13.dp,
                        ).requiredWidth(36.dp)
                        .rotate(14.81f)
                        .border(BorderStroke(2.dp, Color.Black)),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .offset(x = 30.dp, y = 204.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(color = Color(0xffff5686))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "Mi Piso",
                    color = Color.White,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                )
            }
        }

        SelectorDiasSemana(
            fechaSeleccionada = fechaSeleccionada,
            onFechaClick = { nuevaFecha -> viewModel.seleccionarFecha(nuevaFecha) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        val tituloDia =
            if (fechaSeleccionada.isEqual(java.time.LocalDate.now())) {
                "Hoy"
            } else {
                fechaSeleccionada.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
            }

        Text(
            text = tituloDia,
            color = Color.Black,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 30.dp, bottom = 10.dp),
        )

        ListaEventosDelDia(eventos = eventosDelDia)

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "NOTIFICACIONES",
            color = Color.Black,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier.padding(start = 20.dp, bottom = 15.dp),
        )

        Box(
            modifier =
                Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .shadow(4.dp, RoundedCornerShape(10.dp)),
        ) {
            Text(
                text = "Marta te ha pagado 20€",
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier.padding(10.dp).align(Alignment.CenterStart),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "MIS TAREAS PENDIENTES",
            color = Color.Black,
            style = TextStyle(fontSize = 16.sp),
            modifier = Modifier.padding(start = 20.dp, bottom = 15.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Property1otras(modifier = Modifier.padding(horizontal = 20.dp))
            Property1otras(modifier = Modifier.padding(horizontal = 20.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun Property1otras(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .padding(all = 20.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(15.dp)),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = "Tirar basura",
                color = Color.Black,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
            )
            Property1media()
        }

        Text(
            text = "14 Nov",
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}

@Composable
fun Property1media(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .width(70.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(color = Color(0xffddc1fb)),
    ) {
        Text(
            text = "Media",
            color = Color(0xff5d427a),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
        )
    }
}
