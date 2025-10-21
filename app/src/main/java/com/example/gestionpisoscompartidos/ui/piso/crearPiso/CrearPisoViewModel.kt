package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.repositories.Repository
import com.example.gestionpisoscompartidos.model.PisoRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CrearPisoViewModel : ViewModel() {
    private val repository = Repository(NetworkModule.databaseApiService)

    private val _estado = MutableStateFlow<String>("")
    val estado = _estado.asStateFlow()

    fun crearPiso(nombre: String, descripcion: String?, direccion: String?) {
        viewModelScope.launch {
            try {
                val request = PisoRequest(nombre, descripcion, direccion)
                val response = repository.crearPiso(request)
                _estado.value = "✅ Piso creado: ${response.nombre}"
            } catch (e: Exception) {
                _estado.value = "❌ Error: ${e.message}"
            }
        }
    }
}
