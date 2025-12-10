package es.mirumi.es.ui.piso.crearCasa

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.repository.repositories.RepositoryCasa
import es.mirumi.es.model.requests.CasaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CrearCasaViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver
    private val repository = RepositoryCasa(NetworkModule.casaApiService)

    private val _uiState = MutableStateFlow(CrearCasaUiState())
    val uiState: StateFlow<CrearCasaUiState> = _uiState.asStateFlow()

    private val _createFlatResult = MutableStateFlow<Boolean?>(null)
    val createFlatResult: StateFlow<Boolean?> = _createFlatResult

    fun updateNombre(nombre: String) {
        _uiState.value =
            _uiState.value.copy(
                nombre = nombre,
                isButtonEnabled = isButtonEnabled(nombre, _uiState.value.imagenUri),
            )
    }

    fun updateDescripcion(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion)
    }

    fun updateImagenUri(imagenUri: Uri?) {
        _uiState.value =
            _uiState.value.copy(
                imagenUri = imagenUri,
                isButtonEnabled = isButtonEnabled(_uiState.value.nombre, imagenUri),
            )
    }

    private fun isButtonEnabled(
        nombre: String,
        imagenUri: Uri?,
    ): Boolean = nombre.isNotBlank() && imagenUri != null

    suspend fun crearCasa(): Boolean =
        try {
            val casaRequest =
                CasaRequest(
                    nombre = _uiState.value.nombre,
                    descripcion = _uiState.value.descripcion,
                    rutaImagen = null,
                )

            val response = repository.crearCasa(casaRequest, _uiState.value.imagenUri, contentResolver)
            _createFlatResult.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _createFlatResult.value = false
            false
        }
}

data class CrearCasaUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUri: Uri? = null,
    val isButtonEnabled: Boolean = false,
)
