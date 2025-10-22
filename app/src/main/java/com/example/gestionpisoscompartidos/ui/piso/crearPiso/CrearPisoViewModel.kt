package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPiso
import com.example.gestionpisoscompartidos.model.PisoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrearPisoViewModel : ViewModel() {
    // ✅ Usa el repositorio correcto para pisos
    private val repositoryPiso = RepositoryPiso(NetworkModule.pisoApiService)

    // Estado que refleja el resultado (mensaje de éxito o error)
    private val _estado = MutableStateFlow("")
    val estado = _estado.asStateFlow()

    /**
     * Crea un nuevo piso usando los datos proporcionados.
     */
    fun crearPiso(
        nombre: String,
        descripcion: String?,
        direccion: String?,
    ) {
        viewModelScope.launch {
            try {
                val request =
                    PisoRequest(
                        nombre = nombre,
                        descripcion = descripcion,
                        direccion = direccion,
                    )

                // Llamada al backend
                val response = repositoryPiso.crearPiso(request)

                // Actualiza el estado con un mensaje de éxito
                _estado.value = "✅ Piso creado: ${response.nombre}"
            } catch (e: Exception) {
                // En caso de error, muestra el mensaje correspondiente
                _estado.value = "❌ Error: ${e.message ?: "Error desconocido"}"
            }
        }
    }
}
