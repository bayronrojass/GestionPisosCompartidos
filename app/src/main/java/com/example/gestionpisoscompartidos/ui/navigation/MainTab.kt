package com.example.gestionpisoscompartidos.ui.navigation

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.Mail
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryLista
import com.example.gestionpisoscompartidos.ui.gastos.GastosScreen
import com.example.gestionpisoscompartidos.ui.home.HomeScreen
import com.example.gestionpisoscompartidos.ui.home.HomeViewModel
import com.example.gestionpisoscompartidos.ui.home.HomeViewModelFactory
import com.example.gestionpisoscompartidos.ui.listas.ListaScreen
import com.example.gestionpisoscompartidos.ui.listas.ListasViewModel
import com.example.gestionpisoscompartidos.ui.listas.ListasViewModelFactory
import com.example.gestionpisoscompartidos.ui.tareas.TareasScreen
import com.example.gestionpisoscompartidos.ui.tareas.TareasViewModel
import com.example.gestionpisoscompartidos.ui.tareas.TareasViewModelFactory
import com.example.gestionpisoscompartidos.ui.gastos.GastosViewModel
import androidx.compose.material.icons.filled.EuroSymbol
import com.example.gestionpisoscompartidos.ui.gastos.GastosViewModelFactory

@Composable
fun NavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .width(350.dp)
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
                    .requiredWidth(350.dp)
                    .requiredHeight(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black)
                    .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Botón 1 - Home
            NavigationItem(
                icon = Icons.Default.Home,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                padding = 21.dp,
            )

            // Botón 2 - Listas
            NavigationItem(
                icon = Icons.Default.LibraryAddCheck,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                padding = 22.dp,
            )

            // Botón 3 - Tareas
            NavigationItem(
                icon = Icons.Default.Mail,
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                padding = 17.dp,
            )

            // Botón 4 - Perfil
            NavigationItem(
                icon = Icons.Default.AssignmentInd,
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                padding = 12.dp,
            )

            NavigationItem(
                icon = Icons.Default.EuroSymbol, // O AttachMoney
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                padding = 19.dp,
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
                .padding(horizontal = padding, vertical = 14.dp),
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter =
                ColorFilter.tint(
                    if (isSelected) Color.Black else Color.White,
                ),
            modifier = Modifier.size(20.dp),
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

        Text(
            text = "Preview - Tab 0 seleccionado",
            color = Color.White,
        )
    }
}

@Composable
fun MainScreenWithNavigation(
    casaId: Long,
    casaNombre: String,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val savedStateHandle = rememberSaveableStateHolder()
    val context = LocalContext.current.applicationContext

    val sessionManager = remember { SessionManager(context) }
    val repositoryCasa = remember { RepositoryCasa(NetworkModule.casaApiService) }
    val repositoryLista = remember { RepositoryLista(NetworkModule.listaApiService) }

    val homeViewModel: HomeViewModel =
        viewModel(
            factory = HomeViewModelFactory(repositoryCasa, sessionManager, casaId),
        )

    val listaViewModel: ListasViewModel =
        viewModel(
            // factory = ListasViewModelFactory(casaId, repositoryLista, sessionManager)
            factory = ListasViewModelFactory(casaId),
        )

    val tareasViewModel: TareasViewModel =
        viewModel(
            factory = TareasViewModelFactory(casaId),
        )

    val gastosViewModel: GastosViewModel =
        viewModel(
            factory = GastosViewModelFactory(repositoryCasa, sessionManager, casaId),
        )

    val onNavigateToItem: (Long, String) -> Unit = { id, nombre ->
        Log.d("Navegación", "Item clickeado: $id - $nombre")
    }

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
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            savedStateHandle.SaveableStateProvider(selectedTab) {
                when (selectedTab) {
                    0 -> HomeScreen(viewModel = homeViewModel)
                    1 -> ListaScreen(listaViewModel, onNavigateToItem)
                    2 -> TareasScreen(tareasViewModel, casaNombre)
//                    3 -> PerfilTabContent()
                    4 -> GastosScreen(viewModel = gastosViewModel)
                }
            }
        }
    }
}
