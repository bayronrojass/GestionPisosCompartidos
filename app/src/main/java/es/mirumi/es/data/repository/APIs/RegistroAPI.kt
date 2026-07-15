package es.mirumi.es.data.repository.APIs

import es.mirumi.es.model.requests.RegistroRequest
import es.mirumi.es.model.responses.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistroAPI {
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegistroRequest,
    ): Response<LoginResponse>
}
