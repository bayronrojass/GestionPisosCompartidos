package com.example.gestionpisoscompartidos.ui.pizarra
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.PizarraAPI
import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO
import com.example.gestionpisoscompartidos.utils.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PizarraViewModel constructor() : ViewModel() {
    private val puntos: MutableList<PointDeltaDTO> = mutableListOf()
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PizarraAPI::class.java))

    fun add(p: PointDeltaDTO?) {
        if (p != null) puntos.add(p)
    }

    fun save() {
        if (puntos.isEmpty()) return

        viewModelScope.launch {
            val result = repository.request { postDelta(puntos) }
            when (result) {
                is ApiResult.Error -> {
                    Log.d("PizarraViewModel", "Error sending deltas ${result.message}")
                }
                is ApiResult.Success<*> -> {
                    puntos.clear()
                }
                is ApiResult.Throws -> {
                    Log.d("PizarraViewModel", "Throwed sending deltas ${result.exception.message}")
                }
            }
        }
    }

    suspend fun load(): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val result = repository.request { getLienzo() }
                when (result) {
                    is ApiResult.Error -> {
                        Log.d("PizarraViewModel", "Error sending deltas ${result.message}")
                        null
                    }
                    is ApiResult.Success<*> -> {
                        (result.data as ByteArray?)?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    }
                    is ApiResult.Throws -> {
                        Log.d("PizarraViewModel", "Throwed sending deltas ${result.exception.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
