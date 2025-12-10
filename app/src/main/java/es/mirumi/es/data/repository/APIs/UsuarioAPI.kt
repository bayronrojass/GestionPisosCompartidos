package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.Usuario
import es.mirumi.es.model.dtos.UsuarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsuarioAPI {
    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Path("id") id: Long,
    ): Response<Usuario>

    @PUT("usuarios/{id}")
    suspend fun updateUsuario(
        @Path("id") id: Long,
        @Body usuario: UsuarioDTO,
    ): Response<Usuario>

    @DELETE("usuarios/{id}")
    suspend fun deleteUsuario(
        @Path("id") id: Long,
    ): Response<Void>
}
