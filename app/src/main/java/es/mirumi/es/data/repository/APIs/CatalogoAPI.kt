package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.CatalogoProducto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CatalogoAPI {
    @GET("catalogo/buscar")
    suspend fun buscarProductos(
        @Query("q") query: String,
    ): Response<List<CatalogoProducto>>

    @GET("catalogo/populares")
    suspend fun getPopulares(): Response<List<CatalogoProducto>>
}
