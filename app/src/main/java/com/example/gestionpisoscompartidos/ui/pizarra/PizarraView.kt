package com.example.gestionpisoscompartidos.ui.pizarra

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Paint
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.gestionpisoscompartidos.model.Point
import androidx.core.graphics.createBitmap
import com.example.gestionpisoscompartidos.model.dtos.PointDeltaDTO

class PizarraView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private lateinit var bitmap: Bitmap
        private lateinit var canvasBitmap: Canvas
        private lateinit var model: PizarraViewModel
        private val path = Path()
        private var lastPoint: Point? = null
        private val paint =
            Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = 10f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }

        /** Callback que se ejecuta al tocar */
        var onTouchCallback: ((x: Float, y: Float) -> Unit)? = null

        override fun onSizeChanged(
            w: Int,
            h: Int,
            oldw: Int,
            oldh: Int,
        ) {
            super.onSizeChanged(w, h, oldw, oldh)
            bitmap = createBitmap(w, h)
            canvasBitmap = Canvas(bitmap)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            onTouchCallback?.let { it(x, y) }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastPoint = Point(x, y)
                model.add(PointDeltaDTO(x, y, 10f, 1))
                performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastPoint!!.x, lastPoint!!.y, (x + lastPoint!!.x) / 2, (y + lastPoint!!.y) / 2)
                canvasBitmap.drawPath(path, paint)
                lastPoint = Point(x, y)
                model.add(PointDeltaDTO(x, y, 10f, 1))
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                lastPoint = null
                save()
            }
        }
        return true
    }

        override fun performClick(): Boolean {
            super.performClick()
            return true
        }

        private fun save() {
            model.save()
        }

        suspend fun load() {
            canvasBitmap.setBitmap(model.load())
        }

        fun clear() {
            bitmap.eraseColor(Color.TRANSPARENT)
            invalidate()
        }

        fun setModel(newModel: PizarraViewModel) {
            model = newModel
        }
    }
