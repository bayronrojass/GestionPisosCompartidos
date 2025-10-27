package com.example.gestionpisoscompartidos.ui.pizarra
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.set
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.APIs.PizarraAPI
import com.example.gestionpisoscompartidos.model.dtos.DateDTO
import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO
import com.example.gestionpisoscompartidos.utils.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.time.LocalDateTime

class PizarraViewModel : ViewModel() {
    private val puntos: MutableList<PointDeltaDTO> = mutableListOf()
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PizarraAPI::class.java))
    private val _bitmapState = MutableStateFlow<Bitmap?>(null)
    val bitmapState: StateFlow<Bitmap?> = _bitmapState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    val lastLoaded: LocalDateTime = LocalDateTime.MIN

    fun add(p: PointDeltaDTO?) {
        if (p != null) puntos.add(p)
    }

    fun save() {
        if (puntos.isEmpty()) return

        viewModelScope.launch {
            val result = repository.request { postDelta(puntos) }
            when (result) {
                is ApiResult.Error -> {
                    puntos.clear()
                    Log.d("PizarraViewModel", "Error sending deltas ${result.message}")
                }
                is ApiResult.Success<*> -> {
                    puntos.clear()
                }
                is ApiResult.Throws -> {
                    puntos.clear()
                    Log.d("Q", "Throwed sending deltas ${result.exception.message}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun load() =
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val check = repository.request { isUpdated(DateDTO(lastLoaded)) }
                    when (check) {
                        is ApiResult.Error -> {
                            Log.d("PizarraViewModel", "Not need loading")
                        }
                        is ApiResult.Success<*> -> {
                            if (check.data as Boolean) {
                                executeLoading()
                            }
                        }
                        is ApiResult.Throws -> {
                            "Throwed loading ${check.exception.message}"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private suspend fun executeLoading() {
        val result = repository.request { getLienzo() }
        when (result) {
            is ApiResult.Error -> {
                Log.d("PizarraViewModel", "Error loading ${result.message}")
            }

            is ApiResult.Success<*> -> {
                val responseBody = result.data as? ResponseBody
                responseBody?.let { body ->
                    Log.d("PNG", "Content-Type: ${body.contentType()}")
                    val bytes = body.bytes()
                    Log.d("PNG", "Bytes recibidos: ${bytes.size}")

                    if (bytes.isNotEmpty()) {
                        _bitmapState.value =
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size,
                            )
                    }
                }
            }

            is ApiResult.Throws -> {
                Log.d(
                    "PizarraViewModel",
                    "Throwed loading ${result.exception.message}",
                )
            }
        }
    }
}
