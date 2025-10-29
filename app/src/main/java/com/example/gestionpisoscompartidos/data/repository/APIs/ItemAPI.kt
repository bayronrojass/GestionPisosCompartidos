package com.example.gestionpisoscompartidos.data.repository.APIs

import com.example.gestionpisoscompartidos.model.Elemento
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ItemAPI {

    @GET("listas/{listaId}/elementos")
    suspend fun getElementosByListaId(@Path("listaId") listaId: Long): Response<List<Elemento>>

    // @POST("listas/{listaId}/elementos") para añadir
    // @PUT("elementos/{elementoId}") para modificar (marcar como completado)
    // @DELETE("elementos/{elementoId}") para borrar
}