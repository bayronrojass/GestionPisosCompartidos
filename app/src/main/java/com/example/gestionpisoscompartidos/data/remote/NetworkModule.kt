package com.example.gestionpisoscompartidos.data.remote

import com.example.gestionpisoscompartidos.data.repository.APIs.DatabaseAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.LoginAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.CasaAPI
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que gestiona la configuración de Retrofit
 * y proporciona las instancias de los servicios API.
 */
object NetworkModule {
    private const val BASE_URL = "http://localhost:8080/"

    private val gson: Gson =
        GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .create()

    val retrofit: Retrofit by lazy {
        Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provee la instancia del servicio DatabaseAPI.
     * Esta instancia es la que realiza las llamadas HTTP.
     */
    val databaseApiService: DatabaseAPI by lazy {
        retrofit.create(DatabaseAPI::class.java)
    }

    val loginApiService: LoginAPI by lazy {
        retrofit.create(LoginAPI::class.java)
    }

    val casaApiService: CasaAPI by lazy {
        retrofit.create(CasaAPI::class.java)
    }
}
