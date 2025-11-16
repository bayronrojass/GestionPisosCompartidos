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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.gestionpisoscompartidos.R

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .requiredWidth(width = 390.dp)
                .verticalScroll(scrollState)
                .background(color = Color(0xfff8f8f8)),
    ) {
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 27.dp,
                        y = 4.dp,
                    ).requiredWidth(width = 343.dp),
        ) {
//            Image(
//                painter = painterResource(id = R.drawable.home_image24),
//                contentDescription = "image 24",
//                contentScale = ContentScale.Crop,
//                modifier =
//                    Modifier
//                        .requiredWidth(width = 74.dp)
//                        .requiredHeight(height = 40.dp),
//            )
//            Image(
//                painter = painterResource(id = R.drawable.home_image25),
//                contentDescription = "image 25",
//                contentScale = ContentScale.Crop,
//                modifier =
//                    Modifier
//                        .align(alignment = Alignment.TopEnd)
//                        .offset(
//                            x = 0.dp,
//                            y = 0.dp,
//                        ).requiredWidth(width = 88.dp)
//                        .requiredHeight(height = 40.dp),
//            )
        }
        Text(
            text = "Hola, \nNatalia!",
            color = Color.Black,
            lineHeight = 1.em,
            style =
                TextStyle(
                    fontSize = 48.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 31.dp,
                        y = 91.dp,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.home_union),
            contentDescription = "Union",
            modifier =
                Modifier
                    .offset(
                        x = 258.dp,
                        y = 97.dp,
                    ).requiredWidth(width = 92.dp),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector33),
            contentDescription = "Vector 33",
            modifier =
                Modifier
                    .offset(
                        x = 223.dp,
                        y = 102.13.dp,
                    ).requiredWidth(width = 36.dp)
                    .rotate(degrees = 14.81f)
                    .border(border = BorderStroke(2.dp, Color.Black)),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector26),
            contentDescription = "Vector 26",
            modifier =
                Modifier
                    .offset(
                        x = 285.dp,
                        y = 138.88.dp,
                    ).requiredWidth(width = 12.dp)
                    .rotate(degrees = 180f)
                    .border(border = BorderStroke(2.dp, Color.Black)),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector35),
            contentDescription = "Vector 35",
            modifier =
                Modifier
                    .offset(
                        x = 309.dp,
                        y = 138.88.dp,
                    ).requiredWidth(width = 12.dp)
                    .rotate(degrees = 180f)
                    .border(border = BorderStroke(2.dp, Color.Black)),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector30),
            contentDescription = "Vector 30",
            modifier =
                Modifier
                    .offset(
                        x = 315.dp,
                        y = 151.dp,
                    ).requiredWidth(width = 3.dp)
                    .border(border = BorderStroke(2.dp, Color.Black)),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector27),
            contentDescription = "Vector 27",
            modifier =
                Modifier
                    .offset(
                        x = 293.dp,
                        y = 153.dp,
                    ).requiredWidth(width = 24.dp)
                    .border(border = BorderStroke(2.dp, Color.Black)),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector34),
            contentDescription = "Vector 34",
            modifier =
                Modifier
                    .offset(
                        x = 330.dp,
                        y = 160.dp,
                    ).requiredWidth(width = 16.dp)
                    .clip(shape = MaterialTheme.shapes.small)
                    .border(
                        border = BorderStroke(2.dp, Color.Black),
                        shape = MaterialTheme.shapes.small,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector31),
            contentDescription = "Vector 31",
            modifier =
                Modifier
                    .offset(
                        x = 281.dp,
                        y = 181.dp,
                    ).requiredWidth(width = 19.dp)
                    .clip(shape = MaterialTheme.shapes.small)
                    .border(
                        border = BorderStroke(2.dp, Color.Black),
                        shape = MaterialTheme.shapes.small,
                    ),
        )
        Image(
            painter = painterResource(id = R.drawable.home_vector32),
            contentDescription = "Vector 32",
            modifier =
                Modifier
                    .offset(
                        x = 305.dp,
                        y = 181.dp,
                    ).requiredWidth(width = 19.dp)
                    .clip(shape = MaterialTheme.shapes.small)
                    .rotate(degrees = -180f)
                    .border(
                        border = BorderStroke(2.dp, Color.Black),
                        shape = MaterialTheme.shapes.small,
                    ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .offset(
                        x = 30.dp,
                        y = 204.dp,
                    ).clip(shape = RoundedCornerShape(20.dp))
                    .background(color = Color(0xffff5686))
                    .border(
                        border = BorderStroke(1.dp, Color(0xffff5686)),
                        shape = RoundedCornerShape(20.dp),
                    ).padding(
                        horizontal = 10.dp,
                        vertical = 5.dp,
                    ),
        ) {
            Text(
                text = "Calle Utiel, 31",
                color = Color.White,
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
            Image(
                painter = painterResource(id = R.drawable.home_ellipse41),
                contentDescription = "Ellipse 41",
                modifier =
                    Modifier
                        .requiredWidth(width = 11.dp),
            )
        }
        Text(
            text = "NOVIEMBRE ",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 19.dp,
                        y = 278.dp,
                    ),
        )
        Text(
            text = "Vista mensual",
            color = Color.Black,
            textDecoration = TextDecoration.Underline,
            style =
                TextStyle(
                    fontSize = 13.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 283.dp,
                        y = 280.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 71.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 122.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 173.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 224.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 275.dp,
                        y = 320.dp,
                    ),
        )
        Component1(
            modifier =
                Modifier
                    .offset(
                        x = 326.dp,
                        y = 320.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 192.dp,
                        y = 393.dp,
                    ).requiredSize(size = 7.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color(0xffff5686))
                    .border(
                        border = BorderStroke(1.dp, Color(0xffff5686)),
                        shape = CircleShape,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 294.dp,
                        y = 393.dp,
                    ).requiredSize(size = 7.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color(0xffff5686))
                    .border(
                        border = BorderStroke(1.dp, Color(0xffff5686)),
                        shape = CircleShape,
                    ),
        )
        Text(
            text = "Hoy",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 30.dp,
                        y = 428.dp,
                    ).requiredWidth(width = 52.dp),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 460.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color(0xfffff8cf))
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(10.dp),
                    ),
        )
        Text(
            text = "Cenan en casa María y Belén.",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 15.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 54.dp,
                        y = 471.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 32.dp,
                        y = 476.dp,
                    ).requiredSize(size = 7.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color(0xffff5686)),
        )
        Text(
            text = "Sáb",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 29.dp,
                        y = 519.dp,
                    ).requiredWidth(width = 52.dp),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 560.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color.White)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(10.dp),
                    ),
        )
        Text(
            text = "Viene el casero a arreglar la nevera",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 15.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 60.dp,
                        y = 571.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 31.dp,
                        y = 576.dp,
                    ).requiredSize(size = 8.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color(0xffff5686)),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 610.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color.White)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(10.dp),
                    ),
        )
        Text(
            text = "Previa con los bros en casa :p ",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 15.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 60.dp,
                        y = 621.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 31.dp,
                        y = 626.dp,
                    ).requiredSize(size = 8.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color(0xffff5686)),
        )
        Text(
            text = "NOTIFICACIONES",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 685.dp,
                    ).requiredWidth(width = 185.dp),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 730.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 40.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color.White)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(10.dp),
                    ),
        )
        Text(
            text = "Marta te ha pagado 20 ",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 15.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 39.dp,
                        y = 741.dp,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 791.dp,
                    ).requiredWidth(width = 350.dp)
                    .requiredHeight(height = 59.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .background(color = Color.White)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(10.dp),
                    ),
        )
        Text(
            text = "Alguien te recuerda que limpies el baño ",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 15.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 40.dp,
                        y = 801.dp,
                    ).requiredWidth(width = 206.dp),
        )
        Text(
            text = "MIS TAREAS PENDIENTES",
            color = Color.Black,
            style =
                TextStyle(
                    fontSize = 16.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 905.dp,
                    ).requiredWidth(width = 250.dp)
                    .requiredHeight(height = 20.dp),
        )
        Property1otras(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 950.dp,
                    ),
        )
        Property1otras(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 1110.dp,
                    ),
        )
        Property1otras(
            modifier =
                Modifier
                    .offset(
                        x = 20.dp,
                        y = 1294.dp,
                    ),
        )
    }
}

@Composable
fun Component1(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .requiredWidth(width = 45.dp)
                .requiredHeight(height = 70.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .requiredWidth(width = 45.dp)
                    .requiredHeight(height = 70.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(color = Color(0xfffffefe))
                    .border(
                        border = BorderStroke(1.dp, Color(0xffd9d9d9)),
                        shape = RoundedCornerShape(20.dp),
                    ),
        )
        Text(
            text = "16",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 0.dp,
                        y = 10.dp,
                    ).requiredWidth(width = 45.dp)
                    .requiredHeight(height = 27.dp),
        )
        Text(
            text = "Dom",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style =
                TextStyle(
                    fontSize = 12.sp,
                ),
            modifier =
                Modifier
                    .offset(
                        x = 0.dp,
                        y = 40.1.dp,
                    ).requiredWidth(width = 45.dp)
                    .requiredHeight(height = 17.dp),
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
                    .requiredWidth(width = 153.dp),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
            ) {
                Property1media()
            }
        }
        TextField(
            value = "",
            onValueChange = {},
            label = {
                Text(
                    text = "Completar",
                    color = Color(0xff939393),
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
                painter = painterResource(id = R.drawable.home_iconocambio),
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

@Preview(widthDp = 390, heightDp = 1390)
@Composable
private fun HomePreview() {
    HomeScreen(Modifier)
}
