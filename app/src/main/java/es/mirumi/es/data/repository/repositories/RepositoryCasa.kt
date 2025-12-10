package es.mirumi.es.data.repository.repositories

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import es.mirumi.es.data.repository.APIs.CasaAPI
import es.mirumi.es.model.Evento
import es.mirumi.es.model.Gasto
import es.mirumi.es.model.Usuario
import es.mirumi.es.model.requests.CasaRequest
import es.mirumi.es.model.requests.GastoRequest
import es.mirumi.es.model.requests.JoinCasaRequest
import es.mirumi.es.model.responses.CasaDetailsResponseDTO
import es.mirumi.es.model.responses.CasaResponse
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
    ): Response<CasaDetailsResponseDTO> = apiService.getPisoDetails(token, pisoId)

    suspend fun getPisoMiembros(
        token: String,
        pisoId: Long,
    ): Response<List<Usuario>> = apiService.getPisoMiembros(token, pisoId)

    suspend fun removeMiembro(
        token: String,
        casaId: Long,
        usuarioId: Long,
    ): Response<Unit> = apiService.removeMiembro(token, casaId, usuarioId)

    suspend fun joinCasa(
        token: String,
        casaId: Long,
        request: JoinCasaRequest,
    ): Response<String> = apiService.joinCasa(token, casaId, request)

    suspend fun getEventosCasa(
        token: String,
        casaId: Long,
    ): Response<List<Evento>> = apiService.getEventosCasa(token, casaId)

    suspend fun getGastosCasa(
        token: String,
        casaId: Long,
    ): Response<List<Gasto>> = apiService.getGastosCasa(token, casaId)

    suspend fun crearGasto(
        token: String,
        casaId: Long,
        request: GastoRequest,
    ): Response<String> = apiService.postGastoCasa(token, casaId, request)
}
