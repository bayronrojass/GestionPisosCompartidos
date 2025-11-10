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
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import androidx.core.graphics.toColorInt
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryImagen
import com.example.gestionpisoscompartidos.model.dtos.ImagenDTO
import kotlin.math.max

class PostItView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        val postItId: Long,
        private val config: PostItConfig,
    ) : View(context, attrs, defStyleAttr) {
        private var postItColor = Color.YELLOW
        private var topBarColor = "#FFD700".toColorInt() // Dorado
        private var closeButtonColor = Color.RED
        private val bitmapManager = BitmapManager()

        var model: PizarraViewModel? = null
        var view: PizarraView? = null
        private var canBeDragged = true
        private var isDragging = false
        private var dX = 0f
        private var dY = 0f

        val topBarHeight = 60f
        private val closeButtonSize = 60f
        private val closeButtonPadding = 5f

        var isContentVisible = true

        var isCollapseInProgress = false
        var isExpansionInProgress = false

        private var postItBitmap: Bitmap? = null
        private var originalX = 0f
        private var originalY = 0f
        private var originalWidth = 0
        private var originalHeight = 0
        private var isExpanded = false
        private var lastTapTime = 0L

        private var originalPreviewBitmap: Bitmap? = null
        private var loadJob: Job? = null
        private val loadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private val viewScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        var onExpand: ((PostItView) -> Unit)? = null
        var onCollapse: ((PostItView) -> Unit)? = null

        private val repositoryPostIt = RepositoryPostIt()
        private val repositoryImagen = RepositoryImagen()
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isInCloseButton(event.x, event.y) && canBeDragged) {
                        (parent as? ViewGroup)?.removeView(this)
                        remove()
                        return true
                    }

                    if (isInTopBar(event.x, event.y) && canBeDragged) {
                        bringToFront()
                        dX = this.x - event.rawX
                        dY = this.y - event.rawY
                    } else if (isInBody(event.x, event.y)) {
                        expand()
                    }

                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (canBeDragged && isInTopBar(event.x, event.y)) {
                        isDragging = true
                        this.x = event.rawX + dX
                        this.y = event.rawY + dY

                        val parent = parent as? ViewGroup
                        parent?.let {
                            this.x = this.x.coerceIn(0f, it.width - this.width.toFloat())
                            this.y = this.y.coerceIn(0f, it.height - this.height.toFloat())
                        }
                        return true
                    }
                }

                MotionEvent.ACTION_UP -> {
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

                    if (isDragging) {
                        isDragging = false
                        endDrag(this.x, this.y)
                        return true
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
            Log.d("Postit", "$width, $height")
            if (isExpanded || isExpansionInProgress) {
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

                val dstRect = getDrawRect()
                postItBitmap?.let { bitmap ->
                    canvas.drawBitmap(bitmap, null, dstRect, null)
                }
            }
            paint.color = topBarColor
            canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, paint)

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
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)

            val minWidth = (50 * resources.displayMetrics.density).toInt()
            val minHeight = (50 * resources.displayMetrics.density).toInt()

            val finalWidth = max(width, minWidth)
            val finalHeight = max(height, minHeight)

            super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY),
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
                val request =
                    if (!config.isImage) {
                        repositoryPostIt.updatePostItPosition(PostItDTO(postItId, model!!.lienzoId, x, y, 0, 0, !isContentVisible))
                    } else {
                        repositoryImagen.updateImagenPosition(ImagenDTO(postItId, model!!.lienzoId, x, y, 0, 0, !isContentVisible))
                    }
            }
        }

        fun setPreview(bitmap: Bitmap) {
            originalPreviewBitmap = bitmap
            postItBitmap = bitmap
            invalidate()
            requestLayout()
        }

        fun getDrawRect(): Rect =
            Rect(
                closeButtonPadding.toInt(),
                (topBarHeight + closeButtonPadding).toInt(),
                width - closeButtonPadding.toInt(),
                height - closeButtonPadding.toInt(),
            )

        private fun remove() {
            viewScope.launch {
                val response =
                    if (!config.isImage) {
                        repositoryPostIt.deletePostIt(postItId)
                    } else {
                        repositoryImagen.deleteImagen(postItId)
                    }
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

                            val loadResult =
                                async {
                                    model?.load()
                                }.await()

                            val originalBitmap = model?._bitmapState?.value
                            if (originalBitmap != null) {
                                postIt.setPreview(bitmapManager.captureAndScalePreview(originalBitmap, getDrawRect()))
                                postIt.invalidate()
                            }

                            if (config.isImage) {
                                break
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
            val layoutParams =
                this.layoutParams as? FrameLayout.LayoutParams
                    ?: FrameLayout.LayoutParams(width, height)

            layoutParams.width = width
            layoutParams.height = height

            this.layoutParams = layoutParams
            this.x = x
            this.y = y

            requestLayout()
            invalidate()
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
