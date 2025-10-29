package com.example.gestionpisoscompartidos.ui.pizarra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Paint
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.gestionpisoscompartidos.model.Point
import androidx.core.graphics.createBitmap
import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
        private lateinit var currentBitmap: Bitmap
        private lateinit var canvasBitmap: Canvas
        private lateinit var model: PizarraViewModel
        private var backgroundBitmap: Bitmap? = null
        private val path = Path()
        private var lastPoint: Point? = null
        private var saveJob: Job? = null
        private val saveScope = CoroutineScope(Dispatchers.Main)
        private var loadJob: Job? = null
        private val loadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private var paint =
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            currentBitmap = createBitmap(w, h)
            canvasBitmap = Canvas(currentBitmap)
            load()
        }

        fun setBackgroundBitmap(bitmap: Bitmap) {
            backgroundBitmap = bitmap
            currentBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            canvasBitmap = Canvas(currentBitmap)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawBitmap(currentBitmap, 0f, 0f, null)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            val paint = createPaint(model.color)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(x, y)
                    lastPoint = Point(x, y)
                    model.add(PointDeltaDTO(x, y, 10f, model.color))
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
                    model.add(PointDeltaDTO(x, y, 10f, model.color))
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    canvasBitmap.drawPath(path, paint)
                    model.add(PointDeltaDTO(x, y, 0f, model.color))
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

        @RequiresApi(Build.VERSION_CODES.O)
        private fun save() {
            saveJob?.cancel()
            loadJob?.cancel()

            saveJob =
                saveScope.launch {
                    delay(1000L)
                    performActualSave()
                    load()
                }
        }

        private fun performActualSave() {
            model.save()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun load() {
            loadJob?.cancel()

            loadJob =
                loadScope.launch {
                    while (isActive) {
                        try {
                            Log.d("Load", "Cargando...")
                            model.load()
                            delay(3000L)
                        } catch (e: CancellationException) {
                            Log.e("Load", "Error en carga: ${e.message}")
                            break
                        } catch (e: Exception) {
                            Log.e("Load", "Error en carga: ${e.message}")
                            delay(3000L)
                        }
                    }
                }
        }

        fun clear() {
            currentBitmap.eraseColor(Color.TRANSPARENT)
            invalidate()
        }

        fun setModel(newModel: PizarraViewModel) {
            model = newModel
        }

        fun captureBitmap(): Bitmap = currentBitmap
    }
