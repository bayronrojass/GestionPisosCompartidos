package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPiso
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.model.PisoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CrearPisoViewModel : ViewModel() {
    private val repository = RepositoryPiso(NetworkModule.pisoApiService)

    private val _createFlatResult = MutableStateFlow<Boolean?>(null)
    val createFlatResult: StateFlow<Boolean?> = _createFlatResult

    fun nameNull(s: String): Boolean = s.trim().isEmpty()

    fun buttonConditions(
        apartmentName: String,
        pickedPhoto: Uri?,
    ): Boolean = !nameNull(apartmentName) && pickedPhoto != null

    suspend fun createFlat(
        name: String,
        description: String,
        pickedPhoto: Uri,
    ): Boolean =
        try {
            val JavaUri = java.net.URI(pickedPhoto.toString())
            val pisoRequest =
                PisoRequest(
                    nombre = name,
                    descripcion = description,
                    fotoURI = JavaUri.toString(),
                )
            val response = repository.crearPiso(pisoRequest)
            _createFlatResult.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _createFlatResult.value = false
            false
        }
}
