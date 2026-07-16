package es.mirumi.es.ui.pizarra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.scale
import es.mirumi.es.model.Point
import es.mirumi.es.model.dtos.PointDeltaDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class PizarraView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var currentBitmap: Bitmap? = null
        private lateinit var canvasBitmap: Canvas
        internal lateinit var model: PizarraViewModel
        var activatedDraw: Boolean = false
        private var backgroundBitmap: Bitmap? = null
        private val path = Path()
        private var lastPoint: Point? = null
        private val saveScope = CoroutineScope(Dispatchers.Main)
        private val loadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            // Safety-net initialization: allocate a blank white canvas the instant the
            // view has real dimensions, BEFORE any network bitmap arrives. Without this,
            // the ~50-200ms window between layout completion and the cache/network
            // bitmap landing was where touches got dropped by the onTouchEvent crash
            // guard. Any stroke drawn onto this placeholder will be safely replaced by
            // setBackgroundBitmap when the real bitmap arrives — the pendingSave guard
            // there protects against overwriting mid-stroke work.
            if (!::canvasBitmap.isInitialized && w > 0 && h > 0) {
                val placeholder =
                    Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                        eraseColor(Color.WHITE)
                    }
                currentBitmap = placeholder
                canvasBitmap = Canvas(placeholder)
            }
            load()
        }

        /**
         * Applies a bitmap received from the polling `load()` (or from a peer's edit) onto
         * the local canvas. This is a **programmatic** update path — it must never trigger
         * a save cycle, even indirectly. If [save] is ever invoked from anywhere other than
         * a real `ACTION_UP` in [onTouchEvent], the guard in [save] itself will refuse to
         * proceed.
         */
        fun setBackgroundBitmap(bitmap: Bitmap) {
            // Optimistic UI guards — skip applying the incoming bitmap while either:
            //  - a local stroke is pending server acknowledgement (pendingSave) — else the
            //    poll would wipe the freshly-drawn strokes for ~5s, OR
            //  - a server-side clear is in flight (isClearing) — else a poll racing the
            //    clear would restore the pre-clear bitmap onto our locally-blank canvas
            //    before the server has actually persisted the wipe.
            // In both cases we still refresh `backgroundBitmap` as the baseline reference.
            if (::model.isInitialized && (model.pendingSave.value || model.isClearing.value)) {
                backgroundBitmap = bitmap
                return
            }
            if (activatedDraw) {
                backgroundBitmap = bitmap
                currentBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                canvasBitmap = Canvas(currentBitmap!!)
            } else {
                backgroundBitmap = bitmap
                currentBitmap = scaleBitmapToViewSize(bitmap)
                canvasBitmap = Canvas(currentBitmap!!)
            }
            invalidate()
        }

        private fun scaleBitmapToViewSize(bitmap: Bitmap): Bitmap {
            if (width <= 0 || height <= 0) {
                return bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }

            return bitmap.scale(width, height)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // `currentBitmap` is nullable so this `?.let` is inherently safe — no
            // UninitializedPropertyAccessException risk here. onDraw fires many times
            // during measure/layout before any bitmap has arrived; we just draw nothing.
            currentBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (!activatedDraw) {
                return false
            }
            // CRASH GUARD — `canvasBitmap` and `model` are `lateinit`; touch events
            // routinely fire during the split second between the AndroidView factory
            // returning and the first `setBackgroundBitmap` call (fast reopen from
            // `PizarraBitmapCache` hit, or a recomposition triggered by the "Color de
            // la nota" pastel selector swapping the outer sheet's background). Accessing
            // an uninitialized `lateinit` throws `UninitializedPropertyAccessException`
            // straight to the input-event dispatcher, killing the process — see the
            // Logcat trace at `PizarraView.kt:122` (ACTION_MOVE → canvasBitmap.drawPath).
            // Returning `true` here consumes the event silently until the bitmap is
            // ready; the user's next touch (a few frames later) will work normally.
            if (!::model.isInitialized || !::canvasBitmap.isInitialized || currentBitmap == null) {
                return true
            }

            val currentmodel = model
            val x = event.x
            val y = event.y

            model.loadJob?.cancel()
            val paint = createPaint(currentmodel.color)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(x, y)
                    lastPoint = Point(x, y)
                    currentmodel.add(PointDeltaDTO(x, y, 10f, currentmodel.color))
                    performClick()
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    // Extra defense: ACTION_MOVE can arrive without a preceding
                    // ACTION_DOWN if the user was already touching mid-recomposition.
                    // `lastPoint` would be null in that case — fall back to a simple
                    // moveTo so we don't NPE on `lastPoint!!`.
                    val prev =
                        lastPoint ?: run {
                            path.moveTo(x, y)
                            lastPoint = Point(x, y)
                            return true
                        }
                    path.quadTo(
                        prev.x,
                        prev.y,
                        (x + prev.x) / 2,
                        (y + prev.y) / 2,
                    )
                    canvasBitmap.drawPath(path, paint)
                    lastPoint = Point(x, y)
                    currentmodel.add(PointDeltaDTO(x, y, 10f, currentmodel.color))
                    invalidate()
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    canvasBitmap.drawPath(path, paint)
                    currentmodel.add(PointDeltaDTO(x, y, 0f, currentmodel.color))
                    // Mark the local canvas as ahead of the server so incoming polls don't wipe it.
                    currentmodel.markPending()
                    lastPoint = null
                    path.reset()
                    save()
                    return true
                }
            }
            return false
        }

        private fun createPaint(colorByte: Byte): Paint {
            // Brush palette matches the "Color del pincel" selector in the expanded Post-It
            // view. Byte encoding is deliberately compact for the delta wire protocol
            // (`PointDeltaDTO.color: Byte`). Legacy bytes (RED = 2 was pre-redesign) survive
            // through the default `else -> BLACK` branch — no stroke ever crashes on unknown.
            val c =
                when (colorByte) {
                    1.toByte() -> android.graphics.Color.rgb(0xFB, 0xC0, 0x2D) // Yellow  #FBC02D
                    2.toByte() -> android.graphics.Color.rgb(0x38, 0x8E, 0x3C) // Green   #388E3C
                    3.toByte() -> android.graphics.Color.rgb(0x19, 0x76, 0xD2) // Blue    #1976D2
                    4.toByte() -> android.graphics.Color.rgb(0x67, 0x3A, 0xB7) // Purple  #673AB7
                    5.toByte() -> android.graphics.Color.rgb(0xE9, 0x1E, 0x63) // Fuchsia #E91E63
                    6.toByte() -> Color.BLACK
                    7.toByte() -> Color.WHITE
                    else -> Color.BLACK
                }

            return Paint().apply {
                color = c
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
        }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        /**
         * Scheduled ONLY from the `ACTION_UP` branch of [onTouchEvent]. Guarded against any
         * accidental non-touch invocation: without a real touch, `pendingSave` is false and
         * we bail out before scheduling anything.
         *
         * Previously this method ALSO called [load] at the end of the debounced coroutine —
         * effectively restarting the polling loop after every stroke. That coupling (paired
         * with the version-counter tracking's earlier boolean form) was the mechanism behind
         * the "server bombarded with `Se están aplicando deltas` every few seconds even
         * without drawing" bug: overlapping save/load restarts kept the pipeline hot. Polling
         * is already started once from [onSizeChanged] (initial view mount) and refreshed by
         * the `AndroidView` `update` block when `lienzoId` changes — those two entry points
         * are sufficient.
         */
        private fun save() {
            if (!::model.isInitialized) return
            // Physical-touch gate. `pendingSave` is set to true only inside `ACTION_UP` via
            // `model.markPending()`. Any programmatic call site (a lifecycle callback, a
            // recomposition, a bitmap-arrival collector) will observe `false` here and no-op.
            if (!model.pendingSave.value) return

            model.saveJob?.cancel()
            model.saveJob =
                saveScope.launch {
                    delay(1000L)
                    model.save()
                }
        }

        fun load() {
            val currentModel = model
            model.loadJob?.cancel()

            model.loadJob =
                loadScope.launch {
                    // FAST PATH — one network call + IO decode. Populates `_bitmapState` and
                    // toggles `isLoading` for the UI spinner overlay. Skips the redundant
                    // `isUpdated` boolean-RTT that the poll loop would otherwise pay first.
                    try {
                        Log.d("Load", "Initial load ${currentModel.lienzoId}...")
                        currentModel.initialLoad()
                    } catch (e: CancellationException) {
                        Log.e("Load", "Initial load cancelled: ${e.message}")
                        return@launch
                    } catch (e: Exception) {
                        Log.e("Load", "Initial load error: ${e.message}")
                    }

                    // POLL PATH — subsequent iterations use the cheap `isUpdated` shortcut
                    // (most cycles return false and never fetch the full bitmap).
                    while (isActive) {
                        try {
                            delay(5000L)
                            Log.d("Load", "Polling ${currentModel.lienzoId}...")
                            currentModel.load()
                        } catch (e: CancellationException) {
                            Log.e("Load", "Poll cancelled: ${e.message}")
                            break
                        } catch (e: Exception) {
                            Log.e("Load", "Poll error: ${e.message}")
                            delay(5000L)
                        }
                    }
                }
        }

        fun setModel(newModel: PizarraViewModel) {
            model = newModel
        }

        /**
         * Wipe the local canvas to a blank white bitmap of the current view size.
         * Called from the "Borrar" control-panel button.
         *
         * Full-state cleanup — every source of "ghost strokes" is severed:
         *  - Fresh `Bitmap` + `Canvas` + `backgroundBitmap` — the on-screen surface is
         *    truly empty; `onDraw` from now on paints a blank image.
         *  - `path.reset()` + `lastPoint = null` — the in-flight touch path is wiped so
         *    the next `ACTION_MOVE` starts from scratch instead of appending to the
         *    previous stroke's Path object.
         *  - `saveJob?.cancel()` — cancels any debounced save waiting to flush the
         *    pre-clear `puntos` to the server; without this a save queued 300 ms before
         *    the Borrar tap would still race through and re-post the stale strokes.
         *  - `model.clearLocalBuffer()` — drops the ViewModel's queued `puntos` list AND
         *    re-syncs the `pendingSave` version pair (`syncedVersion = dirtyVersion`) so
         *    `setBackgroundBitmap`'s optimistic-UI guard doesn't lock the canvas.
         *  - `PizarraBitmapCache.remove(model.lienzoId)` — guarantees no reopen serves
         *    the pre-clear cached bitmap.
         *
         * NOTE: the current delta-only wire protocol has no explicit "clear canvas"
         * opcode. The server-composited bitmap still holds pre-clear strokes until a
         * dedicated `POST /lienzo/{id}/clear` endpoint is added — flagged as follow-up.
         * For now the ghost only reappears if the user closes the Post-It and reopens
         * it (cache miss → server bitmap fetch → shows the historical strokes). The
         * "immediate reappearance on next stroke" symptom the user reported IS gone.
         */
        fun clearCanvas() {
            if (!::model.isInitialized) return
            val w = width.coerceAtLeast(1)
            val h = height.coerceAtLeast(1)
            val blank =
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.WHITE)
                }
            currentBitmap = blank
            canvasBitmap = Canvas(blank)
            backgroundBitmap = blank
            path.reset()
            lastPoint = null
            model.saveJob?.cancel()
            model.clearLocalBuffer()
            PizarraBitmapCache.remove(model.lienzoId)
            invalidate()
            // Fire the server-side clear so subsequent polls / reopens don't resurrect
            // pre-clear strokes from the server's composited bitmap. Passes our local
            // blank so `clearOnServer` can seed the cache with the exact same bitmap
            // the user is looking at right now — subsequent reopen stays zero-latency.
            model.clearOnServer(blank)
        }
    }
