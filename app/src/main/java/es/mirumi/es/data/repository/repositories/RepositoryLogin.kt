package es.mirumi.es.data.repository.repositories

import es.mirumi.es.model.requests.LoginRequest
import es.mirumi.es.model.responses.LoginResponse
import es.mirumi.es.data.repository.APIs.LoginAPI

class RepositoryLogin(
    private val apiService: LoginAPI,
) {
    suspend fun login(request: LoginRequest): LoginResponse {
        // 1. Realizar la llamada a la API de forma asíncrona
        val response = apiService.login(request)

        // 2. Comprobar si la respuesta HTTP es exitosa (código 2xx)
        if (response.isSuccessful) {
            // 3. Devolver el cuerpo de la respuesta si no es nulo
            return response.body() ?: throw Exception("Respuesta de login vacía o inválida")
        } else {
            // 4. Manejar códigos de error HTTP (ej. 401, 404, 500)
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            val errorMessage =
                when (response.code()) {
                    401 -> "Credenciales incorrectas."
                    404 -> "Endpoint no encontrado en el servidor."
                    else -> "Error del servidor: ${response.code()}. Detalle: $errorBody"
                }
            throw Exception(errorMessage)
        }
    }
}
