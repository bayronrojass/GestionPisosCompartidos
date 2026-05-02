package es.mirumi.es.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.mirumi.es.model.dtos.UsuarioDTO // Asegúrate de que esta ruta sea correcta

// --- DATO DE PRUEBA (Para ver cómo queda visualmente) ---
// Todo esto lo reemplazaremos cuando hagas el GET al Backend
data class UsuarioRanking(
    val posicion: Int,
    val usuario: UsuarioDTO,
    val puntos: Int,
)

val rankingSimulado =
    listOf(
        UsuarioRanking(1, UsuarioDTO(1L, "Natalia", "nat@mail.com"), 250),
        UsuarioRanking(2, UsuarioDTO(2L, "Manolo", "man@mail.com"), 180),
        UsuarioRanking(3, UsuarioDTO(3L, "David", "dav@mail.com"), 120),
        UsuarioRanking(4, UsuarioDTO(4L, "Paula", "pau@mail.com"), 50),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    navController: NavController,
    casaId: Long,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking de Convivencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xfff8f8f8)),
            )
        },
        containerColor = Color(0xfff8f8f8),
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text("¡Mejores compañeros!", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("Puntos totales del piso", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            // --- EL PÓDIUM (Top 3) ---
            PodiumSection(rankingSimulado.take(3))

            Spacer(modifier = Modifier.height(40.dp))

            // --- LISTA (El resto) ---
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(rankingSimulado.drop(3)) { index, item ->
                    RankingListItem(item)
                }
            }
        }
    }
}

@Composable
fun PodiumSection(top3: List<UsuarioRanking>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom, // Se alinean por abajo para que el pódium quede bien
    ) {
        // Segundo lugar (Plata)
        if (top3.size > 1) PodiumItem(top3[1], color = Color(0xFFC0C0C0), height = 120.dp)

        // Primer lugar (Oro) - Más alto
        if (top3.isNotEmpty()) PodiumItem(top3[0], color = Color(0xFFFFD700), height = 160.dp, isWinner = true)

        // Tercer lugar (Bronce)
        if (top3.size > 2) PodiumItem(top3[2], color = Color(0xFFCD7F32), height = 90.dp)
    }
}

@Composable
fun PodiumItem(
    user: UsuarioRanking,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    isWinner: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        // Avatar
        Box(
            modifier =
                Modifier
                    .size(if (isWinner) 70.dp else 50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8061A2))
                    .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                user.usuario.nombre
                    .take(1)
                    .uppercase(),
                color = Color.White,
                fontSize = if (isWinner) 24.sp else 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(user.usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("${user.puntos} pts", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Bloque del Pódium
        Box(
            modifier =
                Modifier
                    .width(70.dp)
                    .height(height)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(color.copy(alpha = 0.8f)),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                "${user.posicion}",
                fontSize = 30.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun RankingListItem(user: UsuarioRanking) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${user.posicion}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray, modifier = Modifier.width(30.dp))

        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF8061A2)), contentAlignment = Alignment.Center) {
            Text(
                user.usuario.nombre
                    .take(1)
                    .uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(user.usuario.nombre, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${user.puntos}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF8061A2))
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF8061A2), modifier = Modifier.size(16.dp))
        }
    }
}
