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
            // Optimistic UI: while a stroke is pending server acknowledgement, refresh the baseline
            // reference but do NOT replace the on-screen bitmap. Otherwise a poll fired seconds
            // before the server processed our delta will wipe the freshly-drawn strokes for ~5s.
            if (::model.isInitialized && model.pendingSave.value) {
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
            currentBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            var currentmodel = model
            if (!activatedDraw) {
                return false
            }
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
                    path.quadTo(
                        lastPoint!!.x,
                        lastPoint!!.y,
                        (x + lastPoint!!.x) / 2,
                        (y + lastPoint!!.y) / 2,
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
            val c =
                when (colorByte) {
                    1.toByte() -> Color.BLACK
                    2.toByte() -> Color.RED
                    3.toByte() -> Color.GREEN
                    4.toByte() -> Color.BLUE
                    8.toByte() -> Color.WHITE
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
                    while (isActive) {
                        try {
                            Log.d("Load", "Cargando ${currentModel.lienzoId}...")
                            currentModel.load()
                            delay(5000L)
                        } catch (e: CancellationException) {
                            Log.e("Load", "Error en carga: ${e.message}")
                            break
                        } catch (e: Exception) {
                            Log.e("Load", "Error en carga: ${e.message}")
                            delay(5000L)
                        }
                    }
                }
        }

        fun setModel(newModel: PizarraViewModel) {
            model = newModel
        }
    }
