package com.example.gestionpisoscompartidos.data.repository

import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.model.LoginResponse
import com.example.gestionpisoscompartidos.model.PisoRequest
import com.example.gestionpisoscompartidos.model.PisoResponse

class Repository(
    private val apiService: DatabaseAPI,
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

    suspend fun crearPiso(request: PisoRequest): PisoResponse {
        val response = apiService.crearPiso(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía al crear piso")
        } else {
            val msg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear piso (${response.code()}): $msg")
        }
    }


}
