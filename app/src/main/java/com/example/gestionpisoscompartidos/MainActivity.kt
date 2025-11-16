package com.example.gestionpisoscompartidos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gestionpisoscompartidos.data.SessionManager
import com.example.gestionpisoscompartidos.ui.navigation.AppNavigation
import com.example.gestionpisoscompartidos.ui.navigation.Route

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        val sessionManager = SessionManager(applicationContext)

        setContent {
            AppNavigation(
                sessionManager = sessionManager,
                startDestination = Route.Login.route,
            )
        }
    }
}
