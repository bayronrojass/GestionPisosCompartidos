package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.PisoAPI
import com.example.gestionpisoscompartidos.model.LoginRequest
import com.example.gestionpisoscompartidos.model.LoginResponse
import com.example.gestionpisoscompartidos.model.PisoRequest
import com.example.gestionpisoscompartidos.model.PisoResponse

class RepositoryPiso(
    private val apiService: PisoAPI,
) {

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