package com.example.gestionpisoscompartidos.data.repository.repositories

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.gestionpisoscompartidos.data.repository.APIs.CasaAPI
import com.example.gestionpisoscompartidos.model.CasaDetailsResponse
import com.example.gestionpisoscompartidos.model.CasaRequest
import com.example.gestionpisoscompartidos.model.CasaResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class RepositoryCasa(
    private val apiService: CasaAPI,
) {
    suspend fun crearCasa(
        request: CasaRequest,
        fileUri: Uri?,
        contentResolver: ContentResolver,
    ): CasaResponse {
        val casaJson = Gson().toJson(request)
        val casaRequestBody = casaJson.toRequestBody("application/json".toMediaTypeOrNull())

        val response =
            if (fileUri != null) {
                val filePart = createFilePart(fileUri, contentResolver)
                apiService.crearCasa(casa = casaRequestBody, file = filePart)
            } else {
                apiService.crearCasa(casa = casaRequestBody, file = null)
            }

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía al crear piso")
        } else {
            val msg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear piso (${response.code()}): $msg")
        }
    }

    private fun createFilePart(
        fileUri: Uri,
        contentResolver: ContentResolver,
    ): MultipartBody.Part =
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            // ... (tu código de createFilePart está bien) ...
            val fileBytes = inputStream.readBytes()
            val mimeType = contentResolver.getType(fileUri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            val filename = "${UUID.randomUUID()}.$extension"
            val fileReqBody = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", filename, fileReqBody)
        } ?: throw IOException("Cannot open file from URI: $fileUri")

    suspend fun getPisoDetails(
        token: String,
        pisoId: Long,
    ): Response<CasaDetailsResponse> = apiService.getPisoDetails(token, pisoId)

    suspend fun removeMiembro(
        token: String,
        casaId: Long,
        usuarioId: Long,
    ): Response<Unit> = apiService.removeMiembro(token, casaId, usuarioId)
}
