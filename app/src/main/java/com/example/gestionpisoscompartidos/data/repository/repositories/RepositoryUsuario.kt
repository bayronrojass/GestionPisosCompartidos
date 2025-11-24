package com.example.gestionpisoscompartidos.data.repository.repositories

import com.example.gestionpisoscompartidos.data.repository.APIs.UsuarioAPI
import com.example.gestionpisoscompartidos.model.Usuario
import com.example.gestionpisoscompartidos.model.dtos.UsuarioDTO

class RepositoryUsuario(
    private val api: UsuarioAPI,
) {
    suspend fun getUsuario(id: Long): Usuario? {
        val response = api.getUsuario(id)
        if (response.isSuccessful) {
            return response.body()
        }
        throw Exception("Error obteniendo usuario: ${response.code()}")
    }

    suspend fun updateUsuario(
        id: Long,
        nombre: String,
        correo: String,
    ): Usuario? {
        // Asumiendo que tu backend espera un objeto con estos campos
        val usuarioDto = UsuarioDTO(id, nombre, correo)
        val response = api.updateUsuario(id, usuarioDto)
        if (response.isSuccessful) {
            return response.body()
        }
        throw Exception("Error actualizando usuario: ${response.code()}")
    }

    suspend fun deleteUsuario(id: Long) {
        val response = api.deleteUsuario(id)
        if (!response.isSuccessful) {
            throw Exception("Error eliminando usuario: ${response.code()}")
        }
    }
}
