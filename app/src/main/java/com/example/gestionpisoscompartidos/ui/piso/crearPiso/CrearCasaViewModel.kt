package com.example.gestionpisoscompartidos.ui.piso.crearCasa

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryCasa
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.model.CasaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CrearCasaViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val contentResolver = application.contentResolver

    private val repository = RepositoryCasa(NetworkModule.casaApiService)
    private val _createFlatResult = MutableStateFlow<Boolean?>(null)
    val createFlatResult: StateFlow<Boolean?> = _createFlatResult

    fun nameNull(s: String): Boolean = s.trim().isEmpty()

    fun buttonConditions(
        apartmentName: String
    ):
        Boolean = !nameNull(apartmentName)

    suspend fun CrearCasa(
        name: String,
        description: String?,
        pickedPhoto: Uri?,
    ): Boolean =
        try {
            val casaRequest =
                CasaRequest(
                    nombre = name,
                    descripcion = description,
                    rutaImagen = null,
                )

            val response = repository.crearCasa(casaRequest, pickedPhoto, contentResolver)
            _createFlatResult.value = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _createFlatResult.value = false
            false
        }
}
