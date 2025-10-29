package com.example.gestionpisoscompartidos.data.remote

import com.example.gestionpisoscompartidos.data.repository.APIs.DatabaseAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.LoginAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.CasaAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.ItemAPI
import com.example.gestionpisoscompartidos.data.repository.APIs.ListaAPI
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que gestiona la configuración de Retrofit
 * y proporciona las instancias de los servicios API.
 */
object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    val retrofit: Retrofit by lazy {
        Builder()
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

    val loginApiService: LoginAPI by lazy {
        retrofit.create(LoginAPI::class.java)
    }

    val casaApiService: CasaAPI by lazy {
        retrofit.create(CasaAPI::class.java)
    }

    val listaApiService: ListaAPI by lazy {
        retrofit.create(ListaAPI::class.java)
    }

    val itemApiService: ItemAPI by lazy {
        retrofit.create(ItemAPI::class.java)
    }
}
