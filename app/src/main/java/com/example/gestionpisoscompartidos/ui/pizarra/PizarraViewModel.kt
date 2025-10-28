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
import java.time.ZoneId

class PizarraViewModel : ViewModel() {
    private val puntos: MutableList<PointDeltaDTO> = mutableListOf()
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PizarraAPI::class.java))
    private val _bitmapState = MutableStateFlow<Bitmap?>(null)
    val bitmapState: StateFlow<Bitmap?> = _bitmapState.asStateFlow()
    var color: Byte = 1

    @RequiresApi(Build.VERSION_CODES.O)
    var lastLoaded: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0)

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
    suspend fun load() {
        try {
            val check =
                withContext(Dispatchers.IO) {
                    val safeTimestamp = lastLoaded.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    repository.request { isUpdated(safeTimestamp) }
                }

            when (check) {
                is ApiResult.Error -> {
                    Log.d("PizarraViewModel", "No need loading: ${check.message}")
                }
                is ApiResult.Success<*> -> {
                    if (check.data as Boolean) {
                        Log.d("PizarraViewModel", "Data updated, loading new content")

                        val result =
                            withContext(Dispatchers.IO) {
                                repository.request { getLienzo() }
                            }

                        when (result) {
                            is ApiResult.Error -> {
                                Log.e("PizarraViewModel", "Error loading: ${result.message}")
                            }
                            is ApiResult.Success<*> -> {
                                val responseBody = result.data as? ResponseBody
                                responseBody?.let { body ->

                                    try {
                                        withContext(Dispatchers.IO) {
                                            val bytes = body.bytes()

                                            if (bytes.isNotEmpty()) {
                                                val bitmap =
                                                    BitmapFactory.decodeByteArray(
                                                        bytes,
                                                        0,
                                                        bytes.size,
                                                    )

                                                if (bitmap != null) {
                                                    withContext(Dispatchers.Main) {
                                                        _bitmapState.value = bitmap
                                                        lastLoaded = LocalDateTime.now()
                                                        Log.d(
                                                            "PizarraViewModel",
                                                            "Image loaded successfully",
                                                        )
                                                    }
                                                } else {
                                                    Log.e(
                                                        "PizarraViewModel",
                                                        "Failed to decode bitmap",
                                                    )
                                                }
                                            } else {
                                                Log.e("PizarraViewModel", "Empty bytes array")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PizarraViewModel", "Error processing image bytes", e)
                                    }
                                } ?: run {
                                    Log.e("PizarraViewModel", "Response body is null")
                                }
                            }
                            is ApiResult.Throws -> {
                                Log.e("PizarraViewModel", "Throwed loading: ${result.exception.message}")
                            }
                        }
                    }
                }
                is ApiResult.Throws -> {
                    Log.e("PizarraViewModel", "Throwed checking updates: ${check.exception.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("PizarraViewModel", "Unexpected error in load function", e)
        }
    }

    fun onColorSelected(c: Byte) {
        color = c
    }
}
