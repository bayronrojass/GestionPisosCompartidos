package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.ItemAPI
import com.example.gestionpisoscompartidos.model.Elemento

class RepositoryItem(private val apiService: ItemAPI) {

    suspend fun getElementosByListaId(listaId: Long): List<Elemento> {
        val response = apiService.getElementosByListaId(listaId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList() // Devuelve lista vacía si el body es null
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al obtener elementos (${response.code()}): $errorBody")
        }
    }
}