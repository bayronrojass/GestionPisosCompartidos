package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Lista
import es.mirumi.es.model.requests.ListaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ListaAPI {
    @GET("listas/casa/{casaId}")
    suspend fun getListasByCasaId(
        @Path("casaId") casaId: Long,
    ): Response<List<Lista>>

    @POST("listas/casa/{casaId}")
    suspend fun crearListaEnCasa(
        @Path("casaId") casaId: Long,
        @Body nuevaLista: ListaRequest,
    ): Response<Lista> // Devuelve la lista creada (con ID)

    @DELETE("listas/{listaId}")
    suspend fun borrarLista(
        @Path("listaId") listaId: Long,
    ): Response<Void>
}
