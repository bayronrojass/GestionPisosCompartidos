package com.example.gestionpisoscompartidos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.model.Evento
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

val ColorRosaFuerte = Color(0xffff5686)
val ColorFondoGris = Color(0xfff8f8f8)
val ColorAmarilloNota = Color(0xfffff8cf)

@Composable
fun SelectorDiasSemana(
    fechaSeleccionada: LocalDate,
    onFechaClick: (LocalDate) -> Unit,
    onVistaMensualClick: () -> Unit,
) {
    val listaDias = (0..30).map { LocalDate.now().plusDays(it.toLong()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = fechaSeleccionada.month.getDisplayName(JavaTextStyle.FULL, Locale("es", "ES")).uppercase(),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            )
            Text(
                text = "Vista mensual",
                style = TextStyle(fontSize = 13.sp, textDecoration = TextDecoration.Underline, color = Color.Black),
                modifier = Modifier.clickable { onVistaMensualClick() },
            )
        }

        // Fila de días
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(listaDias) { fecha ->
                ItemDiaCalendario(
                    fecha = fecha,
                    isSelected = fecha.isEqual(fechaSeleccionada),
                    onClick = { onFechaClick(fecha) },
                )
            }
        }
    }
}

@Composable
fun ItemDiaCalendario(
    fecha: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorBorde = if (isSelected) ColorRosaFuerte else Color(0xffd9d9d9)
    val colorFondo = if (isSelected) ColorRosaFuerte.copy(alpha = 0.1f) else Color(0xfffffefe)

    Box(
        modifier =
            Modifier
                .width(45.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorFondo)
                .border(BorderStroke(1.dp, colorBorde), RoundedCornerShape(20.dp))
                .clickable { onClick() },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = 10.dp),
        ) {
            Text(
                text = fecha.dayOfMonth.toString(),
                style =
                    TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) ColorRosaFuerte else Color.Black,
                    ),
            )
            Text(
                text =
                    fecha.dayOfWeek
                        .getDisplayName(JavaTextStyle.SHORT, Locale("es", "ES"))
                        .replace(".", "")
                        .replaceFirstChar { it.uppercase() },
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
fun ListaEventosDelDia(eventos: List<Evento>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (eventos.isEmpty()) {
            Text(
                text = "No hay eventos para este día",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                modifier = Modifier.padding(start = 40.dp, top = 10.dp),
            )
        } else {
            eventos.forEach { evento -> ItemEventoTimeline(evento) }
        }
    }
}

@Composable
fun ItemEventoTimeline(evento: Evento) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(30.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ColorRosaFuerte))
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ColorAmarilloNota)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column {
                Text(text = evento.nombre, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Black))
                if (!evento.descripcion.isNullOrBlank()) {
                    Text(text = evento.descripcion, style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                }
            }
        }
    }
}

@Composable
fun ListaEventosAgrupados(eventos: List<Evento>) {
    val eventosPorFecha =
        eventos.groupBy {
            try {
                LocalDate.parse(it.fechaInicio.take(10))
            } catch (e: Exception) {
                LocalDate.now()
            }
        }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (eventos.isEmpty()) {
            Text(
                text = "No hay más eventos esta semana",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                modifier = Modifier.padding(start = 40.dp, top = 10.dp),
            )
        } else {
            eventosPorFecha.forEach { (fecha, lista) ->

                val tituloCabecera =
                    when {
                        fecha.isEqual(LocalDate.now()) -> "Hoy"
                        fecha.isEqual(LocalDate.now().plusDays(1)) -> "Mañana"
                        else -> {
                            val diaSemana =
                                fecha.dayOfWeek
                                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale("es", "ES"))
                                    .replace(".", "")
                                    .replaceFirstChar { it.uppercase() }
                            val diaMes = fecha.dayOfMonth
                            "$diaSemana $diaMes"
                        }
                    }

                Text(
                    text = tituloCabecera,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                    modifier = Modifier.padding(top = 8.dp),
                )

                lista.forEach { evento ->
                    ItemEventoTimeline(evento)
                }
            }
        }
    }
}

fun parsearFechaSegura(fechaString: String): LocalDate =
    try {
        java.time.LocalDateTime
            .parse(fechaString, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            .toLocalDate()
    } catch (e: Exception) {
        try {
            LocalDate.parse(fechaString.take(10))
        } catch (e2: Exception) {
            LocalDate.now()
        }
    }
