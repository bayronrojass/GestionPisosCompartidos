package es.mirumi.es

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.ui.navigation.AppNavigation
import es.mirumi.es.ui.navigation.Route

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        NetworkModule.init(applicationContext)

        setContent {
            AppNavigation(
                sessionManager = NetworkModule.sessionManager,
                startDestination = Route.InicioCarga.route,
            )
        }
    }
}
