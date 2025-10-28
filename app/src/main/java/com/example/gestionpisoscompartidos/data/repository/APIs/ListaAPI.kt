package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.Lista
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ListaAPI {
    @GET("casas/{casaId}/listas")
    suspend fun getListasByCasaId(
        @Path("casaId") casaId: Long,
    ): Response<List<Lista>>
}
