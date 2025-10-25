package com.example.gestionpisoscompartidos.data.repository.repositories

import android.content.ContentResolver
import android.net.Uri
import com.example.gestionpisoscompartidos.data.repository.APIs.CasaAPI
import com.example.gestionpisoscompartidos.model.CasaRequest
import com.example.gestionpisoscompartidos.model.CasaResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class RepositoryCasa(
    private val apiService: CasaAPI,
) {
    // La función ahora necesita la Uri y el ContentResolver
    suspend fun crearCasa(
        request: CasaRequest,
        fileUri: Uri,
        contentResolver: ContentResolver,
    ): CasaResponse {
        // 1. Convierte el objeto CasaRequest a JSON y luego a RequestBody
        val casaJson = Gson().toJson(request)
        val casaRequestBody = casaJson.toRequestBody("application/json".toMediaTypeOrNull())

        // 2. Convierte la Uri del archivo a un MultipartBody.Part
        // Lee los bytes del archivo
        val fileStream = contentResolver.openInputStream(fileUri)
        val fileBytes = fileStream!!.readBytes()
        fileStream.close()

        val mimeType = contentResolver.getType(fileUri)
        val fileReqBody = fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())

        val filename = "${UUID.randomUUID()}.jpg"
        val filePart = MultipartBody.Part.createFormData("file", filename, fileReqBody)

        // 3. Llama a la API con las dos partes
        val response = apiService.crearCasa(nuevoPiso = casaRequestBody, file = filePart)

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Respuesta vacía al crear piso")
        } else {
            val msg = response.errorBody()?.string() ?: "Error desconocido"
            throw Exception("Error al crear piso (${response.code()}): $msg")
        }
    }
}
