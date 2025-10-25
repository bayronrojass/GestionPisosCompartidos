package com.example.gestionpisoscompartidos.ui.piso.crearPiso

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPiso
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.repository.APIs.PisoAPI
import com.example.gestionpisoscompartidos.model.PisoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URI
import kotlin.printStackTrace
import kotlin.toString

class CrearPisoViewModel : ViewModel() {
    private val repository = RepositoryPiso(NetworkModule.retrofit.create(PisoAPI::class.java))

    private val _createFlatResult = MutableStateFlow<Boolean?>(null)
    val createFlatResult: StateFlow<Boolean?> = _createFlatResult

    fun nameNull(s: String): Boolean = s.trim().isEmpty()

    fun buttonConditions(
        apartmentName: String
    ): Boolean = !nameNull(apartmentName)

    suspend fun createFlat(
        name: String,
        description: String,
        pickedPhoto: Uri,
    ) {
        try {
            val photoString = if (pickedPhoto != Uri.EMPTY) {
                pickedPhoto.toString()
            } else {
                null
            }

            val descripcionFinal = if (description.trim().isNotEmpty()) name else description

            val pisoRequest = PisoRequest(
                nombre = name,
                descripcion = descripcionFinal,
                fotoURI = photoString
            )

            val response = repository.crearPiso(pisoRequest)

            _createFlatResult.value = true

        } catch (e: Exception) {
            e.printStackTrace()
            _createFlatResult.value = false
        }
    }
}
