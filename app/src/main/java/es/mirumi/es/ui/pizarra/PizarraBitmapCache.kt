package es.mirumi.es.ui.pizarra

import android.graphics.Bitmap
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Process-lifetime in-memory cache for decoded canvas bitmaps, keyed by `lienzoId`.
 *
 * Every entry also stores the `Instant` at which it was populated, so consumers can
 * seed `PizarraViewModel.lastLoaded` from the cache — otherwise the background poll
 * would fire a `getLienzo` on the very first tick (the default `Instant.ofEpochMilli(1000000)`
 * timestamp trivially loses against any server-side modification), wasting the round-trip
 * we just avoided.
 *
 * `ConcurrentHashMap` is used because reads happen from the Main thread (composable
 * accessing via ViewModel) while writes come from `Dispatchers.IO` (after the
 * `fetchAndPublishBitmap` decode). No manual locking required.
 *
 * This object survives the ViewModel lifecycle (VM instances are destroyed when the
 * ExpandedPostIt leaves composition, but the JVM `object` sticks around for the whole
 * app process). Re-opening the same Post-It after navigation hits the cache instantly.
 */
object PizarraBitmapCache {
    private data class Entry(
        val bitmap: Bitmap,
        val loadedAt: Instant,
    )

    private val cache = ConcurrentHashMap<Long, Entry>()

    fun get(lienzoId: Long): Bitmap? = cache[lienzoId]?.bitmap

    fun timestampFor(lienzoId: Long): Instant? = cache[lienzoId]?.loadedAt

    fun put(
        lienzoId: Long,
        bitmap: Bitmap,
    ) {
        cache[lienzoId] = Entry(bitmap, Instant.now())
    }

    /**
     * Evict the cached bitmap for a single canvas. Called eagerly from
     * `PizarraViewModel.save()` — the exact millisecond a local edit is dispatched, the
     * cached pre-edit bitmap is invalidated so no concurrent reopen can ever serve the
     * stale version. The success branch of save then re-populates it via
     * `fetchAndPublishBitmap()` with the freshly-composited server bitmap.
     */
    fun remove(lienzoId: Long) {
        cache.remove(lienzoId)
    }

    /**
     * Drop all cached bitmaps. Call on sign-out to prevent the next user from ever
     * seeing the previous user's drawings. Not currently wired, but exposed for the
     * eventual `SessionManager.logoutUser()` cleanup hook.
     */
    fun clear() {
        cache.clear()
    }
}
