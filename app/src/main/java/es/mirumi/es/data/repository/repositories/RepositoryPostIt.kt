package es.mirumi.es.data.repository.repositories

import android.util.Log
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.remote.RemoteRepository
import es.mirumi.es.data.repository.APIs.PostItAPI
import es.mirumi.es.model.dtos.PostItDTO
import es.mirumi.es.utils.ApiResult
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class RepositoryPostIt {
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PostItAPI::class.java))

    suspend fun createPostIt(
        casaId: Long,
        location: String,
        usuarioId: Long,
    ): PostItDTO? {
        val request = repository.request { crearPostIt(casaId, location, usuarioId) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error creando postit " + request.message + " " + request.code)
                return null
            }

            is ApiResult.Success<PostItDTO> -> {
                Log.e(
                    "Postit",
                    "Creando postit " + request.data.id + " con lienzo " + request.data.lienzoId,
                )
                return request.data
            }

            is ApiResult.Throws -> {
                Log.e("Postit", "Throws creando postit " + request.exception.message)
                return null
            }
        }
    }

    suspend fun getPostIts(
        casaId: Long,
        location: String,
    ): List<PostItDTO>? {
        val request = repository.request { getPostIts(casaId, location) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error recuperando postits " + request.message)
                return null
            }

            is ApiResult.Success<List<PostItDTO>> -> {
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
                Log.e(
                    "Postit",
                    "Throws recuperando detalles de postit " + request.exception.message,
                )
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
                Log.e(
                    "Postit",
                    "Throws actualizando posicion de postit " + request.exception.message,
                )
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

    suspend fun createAudioPostIt(
        casaId: Long,
        location: String,
        archivo: File,
    ): PostItDTO? {
        // Preparamos los datos para ser enviados por red
        val locationBody = location.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = archivo.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", archivo.name, requestFile)

        val request = repository.request { crearPostItAudio(casaId, locationBody, filePart) }

        when (request) {
            is ApiResult.Error -> {
                Log.e("Postit", "Error creando postit de audio: " + request.message + " " + request.code)
                return null
            }
            is ApiResult.Success<PostItDTO> -> {
                Log.d("Postit", "Postit de audio creado con ID: " + request.data.id)
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Postit", "Throws creando postit de audio: " + request.exception.message)
                return null
            }
        }
    }
}
