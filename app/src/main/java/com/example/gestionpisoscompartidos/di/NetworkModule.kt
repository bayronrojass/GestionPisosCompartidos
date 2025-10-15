package com.example.gestionpisoscompartidos.di

import com.example.gestionpisoscompartidos.data.DatabaseAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que gestiona la configuración de Retrofit
 * y proporciona las instancias de los servicios API.
 */
object NetworkModule {

    // IMPORTANTE: Reemplaza con la IP y puerto de tu backend
    private const val BASE_URL = "http://192.168.1.131:8080/api/"

    // Cliente Retrofit configurado (se crea solo una vez)
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provee la instancia del servicio DatabaseAPI.
     * Esta instancia es la que realiza las llamadas HTTP.
     */
    val databaseApiService: DatabaseAPI by lazy {
        retrofit.create(DatabaseAPI::class.java)
    }
}
