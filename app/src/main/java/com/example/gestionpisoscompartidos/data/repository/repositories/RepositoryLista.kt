package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.ListaAPI
import com.example.gestionpisoscompartidos.model.Lista
import com.example.gestionpisoscompartidos.model.ListaRequest

class RepositoryLista(
    private val apiService: ListaAPI,
) {
    suspend fun getListasByCasaId(casaId: Long): List<Lista> {
        val response = apiService.getListasByCasaId(casaId)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta de listas vacía")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al obtener listas (${response.code()}): $errorBody")
        }
    }

    suspend fun crearListaEnCasa(
        casaId: Long,
        request: ListaRequest,
    ): Lista {
        val response = apiService.crearListaEnCasa(casaId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta de creación vacía")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear lista (${response.code()}): $errorBody")
        }
    }
}
