package com.example.gestionpisoscompartidos

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.APIs.DatabaseAPI
import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.utils.ApiResult
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val repository = RemoteRepository(NetworkModule.retrofit.create(DatabaseAPI::class.java))
            val result = repository.request { login(LoginRequest("test", "test")) }

            when (result) {
                is ApiResult.Success<*> -> println("Usuario: ${result.data}")
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

        setContentView(R.layout.activity_main)
    }
}
