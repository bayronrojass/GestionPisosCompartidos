package com.example.gestionpisoscompartidos.data.repository.repositories

import android.util.Log
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.APIs.PostItAPI
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
import com.example.gestionpisoscompartidos.utils.ApiResult

class RepositoryPostIt {
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PostItAPI::class.java))

    suspend fun createPostIt(casaId: Long): PostItDTO? {
        val request = repository.request { crearPostIt(casaId) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error creando postit " + request.message)
                return null
            }
            is ApiResult.Success<PostItDTO> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws creando postit " + request.exception.message)
                return null
            }
        }
    }

    suspend fun getPostIts(casaId: Long): List<Long>? {
        val request = repository.request { getPostIts(casaId) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error recuperando postits " + request.message)
                return null
            }
            is ApiResult.Success<List<Long>> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws recuperando postit " + request.exception.message)
                return null
            }
        }
    }

    suspend fun getPostItDetails(postItId: Long): PostItDTO? {
        val request = repository.request { getPostItDetails(postItId) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error recuperando detalles de postit " + request.message)
                return null
            }
            is ApiResult.Success<PostItDTO> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws recuperando detalles de postit " + request.exception.message)
                return null
            }
        }
    }

    suspend fun updatePostItPosition(postItDTO: PostItDTO): Boolean? {
        val request = repository.request { updatePostItPosition(postItDTO) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error actualizando posicion de postit " + request.message)
                return null
            }
            is ApiResult.Success<Boolean> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws actualizando posicion de postit " + request.exception.message)
                return null
            }
        }
    }

    suspend fun deletePostIt(id: Long): Boolean? {
        val request = repository.request { deletePostIt(id) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error eliminando postit " + request.message)
                return null
            }
            is ApiResult.Success<Boolean> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws eliminando postit " + request.exception.message)
                return null
            }
        }
    }
}
