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
        private var closeButtonColor = Color.RED

        private var canBeDragged = true
        private var isDragging = false
        private var lastTouchX = 0f
        private var lastTouchY = 0f

        val topBarHeight = 60f
        private val closeButtonSize = 60f
        private val closeButtonPadding = 5f

        private var lastTapTime: Long = 0
        private var isContentVisible = true

        private var postItBitmap: Bitmap? = null
        private var originalX = 0f
        private var originalY = 0f
        private var originalWidth = 0
        private var originalHeight = 0
        private var isExpanded = false

        var onExpand: ((PostItView) -> Unit)? = null
        var onCollapse: ((PostItView) -> Unit)? = null

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
                    if (isInCloseButton(x, y) && canBeDragged) {
                        (parent as? ViewGroup)?.removeView(this)
                    }
                    // Verificar si se toca la barra superior
                    else if (isInTopBar(x, y) && canBeDragged) {
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
                    if (isDragging && canBeDragged) {
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
                        if (currentTime - lastTapTime < 1000 && canBeDragged) {
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
                    // CALCULAR srcRect y dstRect EN TIEMPO REAL
                    val srcRect = Rect(0, 0, bitmap.width, bitmap.height)

                    // Calcular dstRect considerando la barra superior y padding
                    val dstRect =
                        Rect(
                            closeButtonPadding.toInt(),
                            (topBarHeight + closeButtonPadding).toInt(),
                            width - closeButtonPadding.toInt(),
                            height - closeButtonPadding.toInt(),
                        )

                    canvas.drawBitmap(bitmap, srcRect, dstRect, null)
                }

                // Dibujar barra superior
                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)
            } else {
                // Dibujar barra superior
                paint.color = topBarColor
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
            if (isExpanded) return

            canBeDragged = false
            originalX = x
            originalY = y
            originalWidth = width
            originalHeight = height

            isExpanded = true
            isDragging = false

            onExpand?.invoke(this)
        }

        fun collapse() {
            if (!isExpanded) return

            canBeDragged = true
            isExpanded = false

            onCollapse?.invoke(this)
        }

        fun getOriginalPosition(): Pair<Float, Float> = Pair(originalX, originalY)

        fun getOriginalSize(): Pair<Int, Int> = Pair(originalWidth, originalHeight)

        fun setPreview(bitmap: Bitmap) {
            postItBitmap = bitmap
            invalidate()
            requestLayout()
        }
    }
