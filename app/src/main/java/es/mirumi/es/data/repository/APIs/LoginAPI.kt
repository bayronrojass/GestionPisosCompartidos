package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.requests.LoginRequest
import es.mirumi.es.model.responses.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAPI {
    @POST("login")
    suspend fun login(
        @Body credenciales: LoginRequest,
    ): Response<LoginResponse>
}
