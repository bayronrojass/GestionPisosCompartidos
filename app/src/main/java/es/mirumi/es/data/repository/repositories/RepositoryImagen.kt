package es.mirumi.es.data.repository.repositories

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.graphics.scale
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.remote.RemoteRepository
import es.mirumi.es.data.repository.APIs.ImagenAPI
import es.mirumi.es.model.dtos.ImagenDTO
import es.mirumi.es.utils.ApiResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID

class RepositoryImagen {
    private val repository = RemoteRepository(NetworkModule.retrofit.create(ImagenAPI::class.java))

    suspend fun createImagen(
        casaId: Long,
        uri: Uri,
        contentResolver: ContentResolver,
    ): ImagenDTO? {
        val request = repository.request { crearImagen(casaId, createFilePart(uri, contentResolver)) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Imagen", "Error creando imagen " + request.message + " " + request.code)
                return null
            }
            is ApiResult.Success<ImagenDTO> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Imagen", "Throws creando imagen " + request.exception.message)
                return null
            }
        }
    }

    suspend fun getImagenes(casaId: Long): List<Long>? {
        Log.d("Imagen", "Cargando imagenes..")
        val request = repository.request { getImagenes(casaId) }
        Log.d("Imagen", "$request")
        when (request) {
            is ApiResult.Error -> {
                Log.e("Imagen", "Error recuperando imagenes " + request.message)
                return null
            }
            is ApiResult.Success<List<Long>> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Imagen", "Throws recuperando imagenes " + request.exception.message)
                return null
            }
        }
    }

    suspend fun getImagenesDetails(postItId: Long): ImagenDTO? {
        val request = repository.request { getImagenDetails(postItId) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Imagen", "Error recuperando detalles de imagen " + request.message)
                return null
            }
            is ApiResult.Success<ImagenDTO> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Imagen", "Throws recuperando detalles de imagen " + request.exception.message)
                return null
            }
        }
    }

    suspend fun updateImagenPosition(imagenDTO: ImagenDTO): Boolean? {
        val request = repository.request { updateImagenPosition(imagenDTO) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Imagen", "Error actualizando posicion de imagen " + request.message)
                return null
            }
            is ApiResult.Success<Boolean> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Imagen", "Throws actualizando posicion de imagen " + request.exception.message)
                return null
            }
        }
    }

    suspend fun deleteImagen(id: Long): Boolean? {
        val request = repository.request { deleteImagen(id) }
        when (request) {
            is ApiResult.Error -> {
                Log.e("Imagen", "Error eliminando imagen " + request.message)
                return null
            }
            is ApiResult.Success<Boolean> -> {
                return request.data
            }
            is ApiResult.Throws -> {
                Log.e("Imagen", "Throws eliminando imagen " + request.exception.message)
                return null
            }
        }
    }

    fun resizeBitmap(
        original: Bitmap,
        minWidth: Int = 300,
        maxWidth: Int = 500,
        maxHeight: Int = 800,
    ): Bitmap {
        var width = original.width
        var height = original.height

        if (width < minWidth) {
            val ratio = minWidth.toFloat() / width
            width = minWidth
            height = (height * ratio).toInt()
        } else if (width > maxWidth) {
            val ratio = maxWidth.toFloat() / width
            width = maxWidth
            height = (height * ratio).toInt()
        }

        if (height > maxHeight) {
            val ratio = maxHeight.toFloat() / height
            height = maxHeight
            width = (width * ratio).toInt()
        }

        return original.scale(width, height)
    }

    private fun createFilePart(
        fileUri: Uri,
        contentResolver: ContentResolver,
    ): MultipartBody.Part {
        Log.d("Image", "uri $fileUri")
        return contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val fileBytes = inputStream.readBytes()
            val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            val filename = "${UUID.randomUUID()}.$extension"
            val fileReqBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", filename, fileReqBody)
        } ?: throw IOException("Cannot open file from URI: $fileUri")
    }
}
