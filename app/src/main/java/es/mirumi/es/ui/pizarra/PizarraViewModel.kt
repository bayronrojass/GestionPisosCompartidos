package es.mirumi.es.ui.pizarra

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.mirumi.es.data.remote.NetworkModule
import es.mirumi.es.data.remote.RemoteRepository
import es.mirumi.es.data.repository.APIs.PizarraAPI
import es.mirumi.es.model.dtos.PointDeltaDTO
import es.mirumi.es.utils.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.time.Instant
import java.time.ZoneId

class PizarraViewModel(
    var lienzoId: Long,
) : ViewModel() {
    private val puntos: MutableList<PointDeltaDTO> = mutableListOf()
    private val repository = RemoteRepository(NetworkModule.retrofit.create(PizarraAPI::class.java))
    val _bitmapState = MutableStateFlow<Bitmap?>(null)
    val bitmapState: StateFlow<Bitmap?> = _bitmapState.asStateFlow()

    /**
     * Optimistic-UI tracking. A simple `Boolean` isn't enough: a stale save's completion
     * can clear the flag while a fresh stroke is already in progress, and the next poll
     * then wipes the local strokes (the "5-second delay on subsequent strokes" bug).
     *
     * We track two monotonic version counters:
     * - [_dirtyVersion] — incremented on every `ACTION_UP` via [markPending]; represents
     *   the high-water mark of local edits the user has committed to the canvas.
     * - [_syncedVersion] — advanced to the dirty snapshot captured *at the moment a save
     *   was dispatched* once that specific save's API call returns. `maxOf` guarantees
     *   monotonicity even when responses arrive out of order.
     *
     * `pendingSave` is `dirty > synced` — true iff there are strokes that have NOT been
     * covered by any completed save yet, regardless of how many saves are in flight.
     */
    private val _dirtyVersion = MutableStateFlow(0L)
    private val _syncedVersion = MutableStateFlow(0L)

    val pendingSave: StateFlow<Boolean> =
        combine(_dirtyVersion, _syncedVersion) { dirty, synced -> dirty > synced }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    var color: Byte = 1
    var saveJob: Job? = null
    var loadJob: Job? = null

    var lastLoaded: Instant = Instant.ofEpochMilli(1000000)

    fun add(p: PointDeltaDTO?) {
        if (p != null) puntos.add(p)
    }

    /**
     * Called on `ACTION_UP` to signal that a stroke has landed on the local canvas.
     * Simply advances the dirty version; the matching acknowledgement comes from [save].
     */
    fun markPending() {
        _dirtyVersion.update { it + 1L }
    }

    fun save() {
        if (puntos.isEmpty()) return

        // Capture BOTH the version we're about to acknowledge AND a private snapshot of the
        // deltas, then clear puntos immediately. Subsequent strokes drawn while this save is
        // in flight now accumulate into a fresh puntos buffer and will not be swallowed by
        // the success-path `.clear()` — a data-loss bug that previously lived here too.
        val versionAtDispatch = _dirtyVersion.value
        val batch = puntos.toList()
        puntos.clear()

        viewModelScope.launch {
            val result = repository.request { postDelta(lienzoId, batch) }
            when (result) {
                is ApiResult.Error ->
                    Log.d("PizarraViewModel", "Error sending deltas ${result.message}")
                is ApiResult.Success<*> -> Unit
                is ApiResult.Throws ->
                    Log.d("Q", "Throwed sending deltas ${result.exception.message}")
            }
            // Advance the acknowledged version. `maxOf` protects against out-of-order responses.
            _syncedVersion.update { maxOf(it, versionAtDispatch) }
        }
    }

    suspend fun load() {
        try {
            val check =
                withContext(Dispatchers.IO) {
                    val safeTimestamp =
                        lastLoaded.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    repository.request { isUpdated(lienzoId, safeTimestamp) }
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
                                repository.request { getLienzo(lienzoId) }
                            }

                        when (result) {
                            is ApiResult.Error -> {
                                Log.e(
                                    "PizarraViewModel",
                                    "Error loading: ${result.message} ${result.code} $lienzoId",
                                )
                            }

                            is ApiResult.Success<*> -> {
                                val responseBody = result.data as? ResponseBody
                                responseBody?.let { body ->

                                    try {
                                        lastLoaded = Instant.now()
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
                                Log.e(
                                    "PizarraViewModel",
                                    "Throwed loading: ${result.exception.message}",
                                )
                            }
                        }
                    }
                }

                is ApiResult.Throws -> {
                    Log.e(
                        "PizarraViewModel",
                        "Throwed checking updates: ${check.exception.message}",
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("PizarraViewModel", "Unexpected error in load function", e)
        }
    }

    fun stop() {
        saveJob?.cancel()
        loadJob?.cancel()
    }
}
