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

    /**
     * True while the initial (or any subsequent forced) bitmap hydration is in flight.
     * Consumers overlay a `CircularProgressIndicator` on the canvas while true, so the
     * user sees an immediate spinner instead of a 3-4s blank canvas on Post-It open.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * True while a server-side clear is in flight. `PizarraView.setBackgroundBitmap`
     * guards on this the same way it guards on [pendingSave] — so any poll fired
     * mid-clear can't restore the pre-clear server bitmap onto our locally-blank
     * canvas before the server has actually persisted the wipe.
     */
    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

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

    /**
     * Wipe every trace of local drawing state — used by `PizarraView.clearCanvas()`
     * when the user taps "Borrar".
     *
     * Without this, `clearCanvas` visually blanks the bitmap but leaves the pre-clear
     * `puntos` buffer intact — so the next `save()` flushes the stale points to the
     * server, the server composites them onto its (still-unblanked) canvas, the poll
     * fetches that bitmap back, and `setBackgroundBitmap` replaces the local blank
     * with the "resurrected" strokes. That's the "ghost effect" the user reports.
     *
     * Advancing `_syncedVersion` to match `_dirtyVersion` flips `pendingSave` back
     * to `false` so the next poll's `setBackgroundBitmap` is free to apply the server
     * bitmap (which we then evict via cache invalidation on the View side). Cache
     * eviction there guarantees no reopen resurrects the pre-clear state either.
     */
    fun clearLocalBuffer() {
        puntos.clear()
        _syncedVersion.value = _dirtyVersion.value
    }

    /**
     * Fire the server-side "Borrar" — asks the backend to wipe the composited bitmap
     * for this `lienzoId` to a blank white surface. Called from `PizarraView.clearCanvas`
     * so the clear is truly permanent: no ghost strokes resurrect on the next stroke
     * (server had been compositing new deltas onto the old bitmap) or on reopen
     * (fetch was returning old bytes).
     *
     * Concurrency contract:
     *  - `_isClearing = true` for the entire duration; `PizarraView.setBackgroundBitmap`
     *    checks this flag and refuses to apply incoming poll bitmaps while true,
     *    protecting the local blank canvas from a mid-flight poll restore.
     *  - On network success: overwrites the cache with the [localBlank] the caller
     *    already computed, advances [lastLoaded] so the follow-up isUpdated check has
     *    an accurate baseline (server bitmap == our blank as of now), and emits the
     *    blank to `_bitmapState` so any observer is in sync.
     *  - On network failure: still keeps `_isClearing = false` on exit (finally block)
     *    so the guard doesn't lock the canvas permanently. Local canvas stays blank
     *    (PizarraView already wiped it), but the server will fight back on next poll
     *    and the user can retry Borrar.
     */
    fun clearOnServer(localBlank: android.graphics.Bitmap) {
        _isClearing.value = true
        viewModelScope.launch {
            try {
                val result = repository.request { clearLienzo(lienzoId) }
                when (result) {
                    is ApiResult.Success<*> -> {
                        PizarraBitmapCache.put(lienzoId, localBlank)
                        lastLoaded = Instant.now()
                        withContext(Dispatchers.Main) {
                            _bitmapState.value = localBlank
                        }
                        Log.d("PizarraViewModel", "Server-side clear succeeded for lienzo $lienzoId")
                    }
                    is ApiResult.Error ->
                        Log.e("PizarraViewModel", "Server clear failed: ${result.message}")
                    is ApiResult.Throws ->
                        Log.e("PizarraViewModel", "Server clear threw: ${result.exception.message}")
                }
            } finally {
                _isClearing.value = false
            }
        }
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

        // PESSIMISTIC CACHE INVALIDATION — happens SYNCHRONOUSLY, before dispatching the
        // network coroutine. Even if `viewModelScope` cancels this launch mid-flight (user
        // minimizes fast after drawing → ExpandedPostIt disposed → VM cleared), the stale
        // cache entry is already gone. The next reopen simply misses the cache and pays a
        // one-time spinner + fresh fetch, guaranteeing correctness. Without this eager
        // eviction, the cache could hold the pre-edit snapshot indefinitely and every
        // subsequent open would serve stale pixels.
        PizarraBitmapCache.remove(lienzoId)

        viewModelScope.launch {
            val result = repository.request { postDelta(lienzoId, batch) }
            when (result) {
                is ApiResult.Error ->
                    Log.d("PizarraViewModel", "Error sending deltas ${result.message}")
                is ApiResult.Success<*> -> Unit
                is ApiResult.Throws ->
                    Log.d("Q", "Throwed sending deltas ${result.exception.message}")
            }
            // Advance the acknowledged version FIRST. `maxOf` protects against out-of-order
            // responses. Doing this before the cache warm below flips `pendingSave` to false
            // (assuming no newer strokes) so the follow-up `fetchAndPublishBitmap` emission
            // is allowed by `setBackgroundBitmap`'s optimistic-UI guard.
            _syncedVersion.update { maxOf(it, versionAtDispatch) }

            if (result is ApiResult.Success<*>) {
                // INSTANT BASELINE UPDATE — the server has just persisted our deltas. Pin
                // `lastLoaded` to now so the next poll cycle's `isUpdated(lienzoId, now)`
                // has an accurate reference point instead of the pre-save timestamp.
                lastLoaded = Instant.now()

                // WARM THE CACHE with the freshly-composited server bitmap. Uses the shared
                // `fetchAndPublishBitmap()` — which writes to the cache on decode success AND
                // emits to `_bitmapState`. The emission is safe: if the user is still on the
                // drawing screen, `setBackgroundBitmap` either applies it (no newer local
                // strokes → visible pixels stay identical because the server has our strokes)
                // or skips it (`pendingSave` still true → newer local strokes preserved).
                // Either way the CACHE is now warm with the correct bitmap for the next
                // reopen, so `initialLoad()` will hit the cache and stay zero-latency.
                try {
                    fetchAndPublishBitmap()
                } catch (e: Exception) {
                    Log.e("PizarraViewModel", "Post-save cache warm failed: ${e.message}")
                }
            }
        }
    }

    /**
     * INSTANT-PATH initial hydration with silent background refresh.
     *
     * Ordering (three tiers, fastest to slowest):
     *   1. **In-memory cache hit** — [PizarraBitmapCache] holds a decoded bitmap for this
     *      `lienzoId`. Emit it immediately, seed [lastLoaded] from the cached timestamp so
     *      the follow-up `isUpdated` check has an accurate baseline (otherwise the default
     *      epoch value would trivially "lose" and trigger a wasted full-bitmap re-fetch),
     *      leave `isLoading = false` from the start (no spinner needed), and dispatch a
     *      **silent background refresh** via [load] — which uses the cheap `isUpdated`
     *      shortcut and only re-fetches + re-emits if the server has actually changed.
     *   2. **Network fetch with spinner** — cache miss. Toggle `isLoading = true`, run the
     *      shared [fetchAndPublishBitmap] (single RTT + IO decode), toggle false when done.
     *      Result gets cached automatically for subsequent opens.
     *   3. (Not this method.) The 5-second poll loop in [load] handles ongoing sync.
     */
    suspend fun initialLoad() {
        val cachedBitmap = PizarraBitmapCache.get(lienzoId)
        if (cachedBitmap != null) {
            _bitmapState.value = cachedBitmap
            _isLoading.value = false

            // Restore lastLoaded from the cache timestamp so the follow-up isUpdated check
            // has a real baseline to compare against. Without this, the default epoch value
            // would cause the isUpdated call to always return true, negating the refresh
            // shortcut and paying the full-bitmap RTT even when nothing changed server-side.
            PizarraBitmapCache.timestampFor(lienzoId)?.let { lastLoaded = it }

            // SILENT BACKGROUND REFRESH — cheap isUpdated boolean check. Only re-fetches
            // the full bitmap if the server has changed since we cached it. Failure is a
            // no-op (cache stays, user keeps seeing the last-known-good drawing).
            viewModelScope.launch {
                try {
                    load()
                } catch (e: Exception) {
                    Log.e("PizarraViewModel", "Silent background refresh failed: ${e.message}")
                }
            }
            return
        }

        // Cache miss — pay the full round-trip and show a spinner.
        _isLoading.value = true
        try {
            fetchAndPublishBitmap()
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Poll-cycle load — the isUpdated shortcut IS worth it here because 90% of poll
     * iterations return `false` (nothing changed) and cost just one cheap boolean RTT.
     * Only fetches the (much larger) bitmap when isUpdated confirms server-side change.
     */
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
                        fetchAndPublishBitmap()
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

    /**
     * Shared network + decode + emit path used by both [initialLoad] and [load].
     * Every heavy operation (network read, byte materialization, `BitmapFactory.decodeByteArray`)
     * runs on [Dispatchers.IO]; only the terminal `_bitmapState.value = …` emission hops to Main.
     */
    private suspend fun fetchAndPublishBitmap() {
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
                if (responseBody == null) {
                    Log.e("PizarraViewModel", "Response body is null")
                    return
                }
                try {
                    lastLoaded = Instant.now()
                    // Bytes read + decode both on IO — decoding a full-canvas PNG can cost
                    // 100-500ms on mid-range devices; doing it on Main would drop frames and
                    // freeze the drawing UI during navigation.
                    val bitmap =
                        withContext(Dispatchers.IO) {
                            val bytes = responseBody.bytes()
                            if (bytes.isEmpty()) {
                                Log.e("PizarraViewModel", "Empty bytes array")
                                null
                            } else {
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                        }

                    if (bitmap != null) {
                        // Cache the fresh bitmap for instant reopen. The next `initialLoad()`
                        // for this `lienzoId` (even on a brand-new ViewModel instance after
                        // navigation) will hit the cache and skip the network entirely.
                        // The bitmap emitted to Main and the one stashed in the cache are
                        // the same object reference — Bitmap is immutable enough for our
                        // read-only render path that sharing is safe (setBackgroundBitmap
                        // does `.copy()` before drawing new strokes onto its own canvas).
                        PizarraBitmapCache.put(lienzoId, bitmap)
                        withContext(Dispatchers.Main) {
                            _bitmapState.value = bitmap
                            Log.d("PizarraViewModel", "Image loaded successfully")
                        }
                    } else {
                        Log.e("PizarraViewModel", "Failed to decode bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("PizarraViewModel", "Error processing image bytes", e)
                }
            }
            is ApiResult.Throws -> {
                Log.e("PizarraViewModel", "Throwed loading: ${result.exception.message}")
            }
        }
    }

    fun stop() {
        saveJob?.cancel()
        loadJob?.cancel()
    }
}
