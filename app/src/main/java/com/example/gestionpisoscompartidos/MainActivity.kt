package com.example.gestionpisoscompartidos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.DatabaseAPI
import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.ui.theme.GestionPisosCompartidosTheme
import com.example.gestionpisoscompartidos.utils.ApiResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val repository = RemoteRepository(NetworkModule.retrofit.create(DatabaseAPI::class.java))
            val result = repository.request { login(LoginRequest("test", "test")) }

            when (result) {
                is ApiResult.Success -> println("Usuario: ${result.data}")
                is ApiResult.Error -> println("Error HTTP ${result.code}: ${result.message}")
                is ApiResult.Throws -> println("Excepción: ${result.exception.message}")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        enableEdgeToEdge()
        setContent {
            GestionPisosCompartidosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GestionPisosCompartidosTheme {
        Greeting("Android")
    }
}
