package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.ItemAPI
import com.example.gestionpisoscompartidos.model.Elemento
import com.example.gestionpisoscompartidos.model.ElementoRequest

class RepositoryItem(
    private val apiService: ItemAPI,
) {
    suspend fun getElementosByListaId(listaId: Long): List<Elemento> {
        val response = apiService.getElementosByListaId(listaId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList() // Devuelve lista vacía si el body es null
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al obtener elementos (${response.code()}): $errorBody")
        }
    }

    suspend fun crearElementoEnLista(
        listaId: Long,
        request: ElementoRequest,
    ): Elemento {
        val response = apiService.crearElementoEnLista(listaId, request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta de creación vacía")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear elemento (${response.code()}): $errorBody")
        }
    }
}
