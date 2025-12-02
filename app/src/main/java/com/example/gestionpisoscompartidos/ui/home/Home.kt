package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.model.Evento
import com.example.gestionpisoscompartidos.ui.pizarra.p2.PizarraScreen

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
) {
    var showFullCalendar by remember { mutableStateOf(false) }
    val fechaSeleccionada by viewModel.fechaSeleccionada.collectAsState()
    val eventosDelDia by viewModel.eventosDelDia.collectAsState()
    val todosLosEventos by viewModel.eventos.collectAsState()

    val onEditEvent = { evento: Evento ->
        // This will be handled in the CalendarioFullView
    }

    if (showFullCalendar) {
        CalendarioFullView(
            fechaSeleccionada = fechaSeleccionada,
            eventos = todosLosEventos,
            onFechaClick = { nuevaFecha -> viewModel.seleccionarFecha(nuevaFecha) },
            onBackClick = { showFullCalendar = false },
            viewModel = viewModel,
        )
    } else {
        val scrollState = rememberScrollState()
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(color = Color(0xfff8f8f8)),
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                Box(modifier = Modifier.offset(x = 27.dp, y = 4.dp).requiredWidth(343.dp)) {}

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
                Image(
                    painter = painterResource(id = R.drawable.home_vector26),
                    contentDescription = "Vector 26",
                    modifier =
                        Modifier
                            .offset(
                                x = 285.dp,
                                y = 138.88.dp,
                            ).requiredWidth(12.dp)
                            .rotate(180f)
                            .border(BorderStroke(2.dp, Color.Black)),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector35),
                    contentDescription = "Vector 35",
                    modifier =
                        Modifier
                            .offset(
                                x = 309.dp,
                                y = 138.88.dp,
                            ).requiredWidth(12.dp)
                            .rotate(180f)
                            .border(BorderStroke(2.dp, Color.Black)),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector30),
                    contentDescription = "Vector 30",
                    modifier = Modifier.offset(x = 315.dp, y = 151.dp).requiredWidth(3.dp).border(BorderStroke(2.dp, Color.Black)),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector27),
                    contentDescription = "Vector 27",
                    modifier = Modifier.offset(x = 293.dp, y = 153.dp).requiredWidth(24.dp).border(BorderStroke(2.dp, Color.Black)),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector34),
                    contentDescription = "Vector 34",
                    modifier =
                        Modifier
                            .offset(
                                x = 330.dp,
                                y = 160.dp,
                            ).requiredWidth(
                                16.dp,
                            ).clip(MaterialTheme.shapes.small)
                            .border(BorderStroke(2.dp, Color.Black), MaterialTheme.shapes.small),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector31),
                    contentDescription = "Vector 31",
                    modifier =
                        Modifier
                            .offset(
                                x = 281.dp,
                                y = 181.dp,
                            ).requiredWidth(
                                19.dp,
                            ).clip(MaterialTheme.shapes.small)
                            .border(BorderStroke(2.dp, Color.Black), MaterialTheme.shapes.small),
                )
                Image(
                    painter = painterResource(id = R.drawable.home_vector32),
                    contentDescription = "Vector 32",
                    modifier =
                        Modifier
                            .offset(
                                x = 305.dp,
                                y = 181.dp,
                            ).requiredWidth(
                                19.dp,
                            ).clip(MaterialTheme.shapes.small)
                            .rotate(-180f)
                            .border(BorderStroke(2.dp, Color.Black), MaterialTheme.shapes.small),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .offset(
                                x = 30.dp,
                                y = 204.dp,
                            ).clip(RoundedCornerShape(20.dp))
                            .background(color = Color(0xffff5686))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "Mi Piso",
                        color = Color.White,
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    )
                    Image(
                        painter = painterResource(id = R.drawable.home_ellipse41),
                        contentDescription = "Ellipse 41",
                        modifier = Modifier.requiredWidth(11.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SelectorDiasSemana(
                fechaSeleccionada = fechaSeleccionada,
                viewModel = viewModel,
                onFechaClick = { nuevaFecha -> viewModel.seleccionarFecha(nuevaFecha) },
                onVistaMensualClick = { showFullCalendar = true },
            )

            Spacer(modifier = Modifier.height(10.dp))

            ListaEventosAgrupados(
                eventos = eventosDelDia,
                viewModel = viewModel,
                onEditEvent = onEditEvent,
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                "NOTIFICACIONES",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 20.dp, bottom = 15.dp),
            )

            Box(
                modifier =
                    Modifier
                        .padding(
                            horizontal = 20.dp,
                        ).fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .shadow(1.dp),
            ) {
                Text("Marta te ha pagado 20€", modifier = Modifier.padding(10.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "MIS TAREAS PENDIENTES",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 20.dp, bottom = 15.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Property1otras(modifier = Modifier.padding(horizontal = 20.dp))
                Property1otras(modifier = Modifier.padding(horizontal = 20.dp))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        PizarraScreen(location = "Home")
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
