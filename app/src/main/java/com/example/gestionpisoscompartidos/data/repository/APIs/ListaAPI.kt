package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.Lista
import com.example.gestionpisoscompartidos.model.ListaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ListaAPI {
    @GET("casas/{casaId}/listas")
    suspend fun getListasByCasaId(
        @Path("casaId") casaId: Long,
    ): Response<List<Lista>>

    @POST("casas/{casaId}/listas")
    suspend fun crearListaEnCasa(
        @Path("casaId") casaId: Long,
        @Body nuevaLista: ListaRequest,
    ): Response<Lista> // Devuelve la lista creada (con ID)
}
