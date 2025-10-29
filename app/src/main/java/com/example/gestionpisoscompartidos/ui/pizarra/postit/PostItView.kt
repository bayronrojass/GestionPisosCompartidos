package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

class PostItView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        private var postItColor = Color.YELLOW
        private var topBarColor = Color.parseColor("#FFD700") // Dorado
        private var borderColor = Color.BLACK
        private var closeButtonColor = Color.RED

        private var isDragging = false
        private var lastTouchX = 0f
        private var lastTouchY = 0f

        private val topBarHeight = 60f
        private val closeButtonSize = 60f
        private val closeButtonPadding = 5f

        private var lastTapTime: Long = 0
        private var isContentVisible = true

        private var postItBitmap: Bitmap? = null
        private var isExpanded = false

        var onExpand: ((PostItView) -> Unit)? = null
        var onCollapse: ((PostItView, Bitmap?) -> Unit)? = null

        val srcRect = postItBitmap?.let { Rect(0, 0, it.width, it.height) }
        val dstRect =
            Rect(
                closeButtonPadding.toInt(),
                topBarHeight.toInt() + closeButtonPadding.toInt(),
                width - closeButtonPadding.toInt(),
                height - closeButtonPadding.toInt(),
            )

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 24f
                textAlign = Paint.Align.CENTER
            }

        init {
            setOnTouchListener { _, event -> handleTouch(event) }
        }

        private fun handleTouch(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Verificar si se toca la X de cerrar
                    if (isInCloseButton(x, y)) {
                        (parent as? ViewGroup)?.removeView(this)
                    }
                    // Verificar si se toca la barra superior
                    else if (isInTopBar(x, y)) {
                        lastTouchX = x
                        lastTouchY = y
                        isDragging = true
                        this.bringToFront()
                    }
                    // Verificar si se toca el cuerpo
                    else if (isInBody(x, y)) {
                        expand()
                    }

                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY

                        translationX += dx
                        translationY += dy

                        lastTouchX = x
                        lastTouchY = y
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    isDragging = false

                    if (isInTopBar(x, y)) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 1000) {
                            isContentVisible = !isContentVisible
                            lastTapTime = 0
                            invalidate()
                            return true
                        }
                        lastTapTime = currentTime
                    }
                }
            }
            return false
        }

        private fun isInTopBar(
            x: Float,
            y: Float,
        ): Boolean = y <= topBarHeight

        private fun isInBody(
            x: Float,
            y: Float,
        ): Boolean = y > topBarHeight && !isInCloseButton(x, y)

        private fun isInCloseButton(
            x: Float,
            y: Float,
        ): Boolean =
            x >= width - closeButtonSize - closeButtonPadding &&
                y <= closeButtonSize + closeButtonPadding

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isContentVisible) {
                // Dibujar cuerpo principal
                paint.color = postItColor
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                postItBitmap?.let { bitmap ->
                    canvas.drawBitmap(bitmap, srcRect, dstRect, null)
                }

                // Dibujar barra superior
                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)

                // Dibujar borde
                paint.color = borderColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            } else {
                // Dibujar barra superior
                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)

                // Dibujar borde
                paint.color = borderColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)
            }

            // Dibujar botón de cerrar (X)
            paint.color = closeButtonColor
            paint.style = Paint.Style.FILL
            val closeButtonLeft = width - closeButtonSize - closeButtonPadding
            canvas.drawRect(
                closeButtonLeft,
                closeButtonPadding,
                closeButtonLeft + closeButtonSize,
                closeButtonPadding + closeButtonSize,
                paint,
            )

            // Dibujar la X blanca
            textPaint.color = Color.WHITE
            canvas.drawText(
                "X",
                closeButtonLeft + closeButtonSize / 2,
                closeButtonPadding + closeButtonSize / 2 + textPaint.textSize / 3,
                textPaint,
            )
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val desiredWidth = (100 * resources.displayMetrics.density).toInt()
            val desiredHeight = (100 * resources.displayMetrics.density).toInt()

            setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec),
            )
        }

        fun expand() {
            isExpanded = true
            visibility = GONE
            onExpand?.invoke(this)
        }

        fun collapse(bitmap: Bitmap) {
            isExpanded = false
            postItBitmap = bitmap
            visibility = View.VISIBLE
            invalidate()
            onCollapse?.invoke(this, bitmap)
        }

        fun setPreview(bitmap: Bitmap) {
            postItBitmap = bitmap
            invalidate()
        }
    }
