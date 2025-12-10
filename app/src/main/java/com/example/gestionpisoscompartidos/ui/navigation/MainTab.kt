package com.example.gestionpisoscompartidos.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.ui.eventos.EventosViewModel
import com.example.gestionpisoscompartidos.ui.gastos.GastosScreen
import com.example.gestionpisoscompartidos.ui.gastos.GastosViewModel
import com.example.gestionpisoscompartidos.ui.gastos.GastosViewModelFactory
import com.example.gestionpisoscompartidos.ui.home.HomeScreen
import com.example.gestionpisoscompartidos.ui.home.HomeViewModel
import com.example.gestionpisoscompartidos.ui.home.HomeViewModelFactory
import com.example.gestionpisoscompartidos.ui.listas.ListaScreen
import com.example.gestionpisoscompartidos.ui.listas.ListasViewModel
import com.example.gestionpisoscompartidos.ui.listas.ListasViewModelFactory
import com.example.gestionpisoscompartidos.ui.perfil.PerfilScreen
import com.example.gestionpisoscompartidos.ui.tareas.TareasScreen
import com.example.gestionpisoscompartidos.ui.tareas.TareasViewModel
import com.example.gestionpisoscompartidos.ui.tareas.TareasViewModelFactory

@Composable
fun MainScreenWithNavigation(
    casaId: Long,
    casaNombre: String,
    onNavigateToItem: (Long, String) -> Unit,
    onLogout: (String) -> Unit,
    onNavigateToMonthlyView: () -> Unit, // Add this parameter
) {
    var selectedTab by remember { mutableIntStateOf(2) }
    val savedStateHandle = rememberSaveableStateHolder()
    val context = LocalContext.current.applicationContext

    val sessionManager = remember { SessionManager(context) }
    val repositoryCasa = remember { RepositoryCasa(NetworkModule.casaApiService) }
    val contentResolver = LocalContext.current.applicationContext.contentResolver

    val homeViewModel: HomeViewModel =
        viewModel(
            factory =
                HomeViewModelFactory(
                    context = context,
                    sessionManager = sessionManager,
                    casaId = casaId,
                ),
        )

    val listaViewModel: ListasViewModel =
        viewModel(
            factory = ListasViewModelFactory(casaId),
        )

    val eventosViewModel: EventosViewModel =
        viewModel()

    val tareasViewModel: TareasViewModel =
        viewModel(
            factory = TareasViewModelFactory(casaId),
        )

    val gastosViewModel: GastosViewModel =
        viewModel(
            factory = GastosViewModelFactory(repositoryCasa, sessionManager, casaId),
        )

    Scaffold(
        bottomBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                NavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    // No need for onNavigateToMonthlyView here
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            savedStateHandle.SaveableStateProvider(selectedTab) {
                when (selectedTab) {
                    0 -> GastosScreen(viewModel = gastosViewModel, casaId = casaId)
                    1 -> TareasScreen(tareasViewModel, casaNombre, casaId = casaId)
                    2 ->
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToMonthlyView = onNavigateToMonthlyView, // Pass it here
                        )
                    3 -> ListaScreen(listaViewModel, onNavigateToItem, casaId = casaId)
                    4 -> PerfilScreen(sessionManager, onLogout)
                }
            }
        }
    }
}

@Composable
fun NavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    // Remove onNavigateToMonthlyView from here - it doesn't need it
) {
    val barWidth = 380.dp

    Card(
        modifier =
            modifier
                .width(barWidth)
                .height(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(30.dp),
                    clip = false,
                ),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                modifier
                    .requiredWidth(barWidth)
                    .requiredHeight(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black)
                    .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Boton 1: Pagos
            NavigationItem(
                icon = ImageVector.vectorResource(id = R.drawable.property_1_tarjetaa),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                padding = 12.dp,
            )

            // Botón 2: Tareas
            NavigationItem(
                icon = ImageVector.vectorResource(id = R.drawable.tareas_icon),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                padding = 12.dp,
            )

            // Botón 3: Home
            NavigationItem(
                icon = ImageVector.vectorResource(id = R.drawable.casita),
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                padding = 12.dp,
            )

            // Botón 4: Listas
            NavigationItem(
                icon = ImageVector.vectorResource(id = R.drawable.exclude),
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                padding = 12.dp,
            )

            // Botón 5: Usuario
            NavigationItem(
                icon = ImageVector.vectorResource(id = R.drawable.icono_user),
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                padding = 12.dp,
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    padding: Dp,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) Color.White else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = padding, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter =
                ColorFilter.tint(
                    if (isSelected) Color.Black else Color.White,
                ),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview
@Composable
fun NavigationBarPreview() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Gray)
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NavigationBar(
            selectedTab = 0,
            onTabSelected = { },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Preview - Tab 0 seleccionado", color = Color.White)
    }
}

@Preview
@Composable
fun MainScreenWithNavigationPreview() {
    MainScreenWithNavigation(
        casaId = 1L,
        casaNombre = "Casa de prueba",
        onNavigateToItem = { _, _ -> },
        onLogout = {},
        onNavigateToMonthlyView = {},
    )
}
