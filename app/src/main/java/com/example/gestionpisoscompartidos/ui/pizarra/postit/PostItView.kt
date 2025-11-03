package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraView
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs

class PostItView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        val postItId: Long,
    ) : View(context, attrs, defStyleAttr) {
        private var postItColor = Color.YELLOW
        private var topBarColor = Color.parseColor("#FFD700") // Dorado
        private var closeButtonColor = Color.RED
        private val bitmapManager = BitmapManager()

        var model: PizarraViewModel? = null
        var view: PizarraView? = null
        private var canBeDragged = true
        private var isDragging = false
        private var startX = 0f
        private var startY = 0f
        private val touchSlop = 20

        val topBarHeight = 60f
        private val closeButtonSize = 60f
        private val closeButtonPadding = 5f

        private var lastTapTime: Long = 0
        var isContentVisible = true

        var isCollapseInProgress = false
        var isExpansionInProgress = false

        private var postItBitmap: Bitmap? = null
        private var originalX = 0f
        private var originalY = 0f
        private var originalWidth = 0
        private var originalHeight = 0
        private var isExpanded = false

        private var originalPreviewBitmap: Bitmap? = null
        private var loadJob: Job? = null
        private val loadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        var onExpand: ((PostItView) -> Unit)? = null
        var onCollapse: ((PostItView) -> Unit)? = null

        private val repositoryPostIt = RepositoryPostIt()
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
                    startX = x
                    startY = y

                    if (isInCloseButton(x, y) && canBeDragged) {
                        (parent as? ViewGroup)?.removeView(this)
                        remove()
                    } else if (isInTopBar(x, y) && canBeDragged) {
                        this.bringToFront()
                    } else if (isInBody(x, y)) {
                        expand()
                    }
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (canBeDragged && isInTopBar(startX, startY)) {
                        val dx = x - startX
                        val dy = y - startY

                        if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                            isDragging = true
                        }

                        if (isDragging) {
                            val parent = parent as? ViewGroup
                            val absoluteX = event.rawX - (parent?.x ?: 0f)
                            val absoluteY = event.rawY - (parent?.y ?: 0f)
                            this.x = absoluteX - width / 2
                            this.y = absoluteY - height / 2
                        }
                    }
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        isDragging = false
                        endDrag(this.x, this.y)
                        return true
                    }

                    if (isInTopBar(x, y)) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 500) {
                            isContentVisible = !isContentVisible
                            endDrag(this.x, this.y)
                            invalidate()
                            lastTapTime = 0
                        } else {
                            lastTapTime = currentTime
                        }
                    }
                    return true
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

            if (isExpanded) {
                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)

                paint.color = closeButtonColor
                val closeButtonLeft = width - closeButtonSize - closeButtonPadding
                canvas.drawRect(
                    closeButtonLeft,
                    closeButtonPadding,
                    closeButtonLeft + closeButtonSize,
                    closeButtonPadding + closeButtonSize,
                    paint,
                )

                textPaint.color = Color.WHITE
                canvas.drawText(
                    "X",
                    closeButtonLeft + closeButtonSize / 2,
                    closeButtonPadding + closeButtonSize / 2 + textPaint.textSize / 3,
                    textPaint,
                )
                return
            }

            if (isContentVisible) {
                paint.color = postItColor
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                postItBitmap?.let { bitmap ->
                    val scaledBitmap = bitmap
                    val dstRect =
                        Rect(
                            closeButtonPadding.toInt(),
                            (topBarHeight + closeButtonPadding).toInt(),
                            width - closeButtonPadding.toInt(),
                            height - closeButtonPadding.toInt(),
                        )
                    canvas.drawBitmap(scaledBitmap, null, dstRect, null)
                }

                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)
            } else {
                paint.color = topBarColor
                canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)
            }

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
            if (isExpanded || isExpansionInProgress) return

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
            if (!isExpanded || isExpansionInProgress || isCollapseInProgress) return

            view?.saveJob?.cancel()
            model?.save()

            loadJob?.cancel()
            canBeDragged = true
            isExpanded = false

            onCollapse?.invoke(this)
        }

        fun getOriginalPosition(): Pair<Float, Float> = Pair(originalX, originalY)

        fun getOriginalSize(): Pair<Int, Int> = Pair(originalWidth, originalHeight)

        fun endDrag(
            x: Float,
            y: Float,
        ) {
            viewScope.launch {
                val request = repositoryPostIt.updatePostItPosition(PostItDTO(postItId, model!!.lienzoId, x, y, !isContentVisible))
            }
        }

        fun setPreview(bitmap: Bitmap) {
            originalPreviewBitmap = bitmap
            postItBitmap = bitmap
            invalidate()
            requestLayout()
        }

        private fun remove() {
            viewScope.launch {
                val response = repositoryPostIt.deletePostIt(postItId)
            }
            onDetachedFromWindow()
            loadJob?.cancel()
        }

        fun load() {
            loadJob?.cancel()
            val postIt = this
            loadJob =
                loadScope.launch {
                    while (isActive) {
                        try {
                            Log.d("Load", "Cargando PostIt${model?.lienzoId}...")
                            model?.load()

                            val originalBitmap = model?._bitmapState?.value
                            if (originalBitmap != null) {
                                bitmapManager.captureAndScalePreview(originalBitmap, postIt)
                            }

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

        fun setupLayout(
            width: Int,
            height: Int,
            x: Float,
            y: Float,
        ) {
            val layoutParams = FrameLayout.LayoutParams(width, height)
            this.layoutParams = layoutParams
            this.x = x
            this.y = y
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            loadJob?.cancel()
            postItBitmap?.recycle()
            postItBitmap = null
            originalPreviewBitmap?.recycle()
            originalPreviewBitmap = null
        }
    }
