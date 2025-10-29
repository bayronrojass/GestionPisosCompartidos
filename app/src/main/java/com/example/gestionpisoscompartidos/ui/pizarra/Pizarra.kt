package com.example.gestionpisoscompartidos.ui.pizarra

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostItView
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.scale

open class Pizarra : Fragment() {
    companion object {
        fun newInstance() = Pizarra()
    }

    private val viewModel: PizarraViewModel by viewModels()
    private var drawView: PizarraView? = null
    private lateinit var postItContainer: FrameLayout
    private lateinit var buttonContainer: LinearLayout
    private lateinit var mainContainer: ConstraintLayout

    private var currentExpandedPostIt: PostItView? = null
    private var postItOverlay: View? = null
    private var expandedPizarraView: PizarraView? = null
    private var expandedPizarraViewModel: PizarraViewModel? = null
    private val closeButtonPadding = 5f * Resources.getSystem().displayMetrics.density

    private var isInPostItMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_pizarra, container, false)
        drawView = rootView.findViewById(R.id.pizarraView)
        buttonContainer = rootView.findViewById(R.id.buttonContainer)
        postItContainer = rootView.findViewById(R.id.postItContainer)
        mainContainer = rootView.findViewById(R.id.mainContainer)

        buttonContainer.bringToFront()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        drawView?.setModel(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bitmapState.collect { bitmap ->
                bitmap?.let {
                    drawView?.setBackgroundBitmap(it)
                }
            }
        }

        setupClickListeners(view)

        drawView?.load()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners(view: View) {
        view.findViewById<Button>(R.id.btnBlack).setOnClickListener {
            viewModel.onColorSelected(1)
        }

        view.findViewById<Button>(R.id.btnRed).setOnClickListener {
            viewModel.onColorSelected(2)
        }

        view.findViewById<Button>(R.id.btnBlue).setOnClickListener {
            viewModel.onColorSelected(4)
        }

        view.findViewById<Button>(R.id.btnGreen).setOnClickListener {
            viewModel.onColorSelected(3)
        }

        view.findViewById<Button>(R.id.btnWhite).setOnClickListener {
            viewModel.onColorSelected(8)
        }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (isInPostItMode) {
                // captureAndCollapsePostIt()
            }
        }

        view.findViewById<Button>(R.id.btnUndo).setOnClickListener {
            addNewPostIt(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNewPostIt(view: View) {
        val postIt =
            PostItView(requireContext()).apply {
                val layoutParams = FrameLayout.LayoutParams(300, 300)
                this.layoutParams = layoutParams
                x = 100f
                y = 100f

                onExpand = { expandedPostIt ->
                    expandPostIt(expandedPostIt)
                }

                onCollapse = { collapsedPostIt ->
                    collapsePostIt(collapsedPostIt)
                }
            }

        postItContainer.addView(postIt)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun expandPostIt(postIt: PostItView) {
        if (currentExpandedPostIt != null) return

        currentExpandedPostIt = postIt

        createOverlay()

        animateExpansion(postIt)
    }

    private fun createOverlay() {
        postItOverlay =
            View(requireContext()).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(Color.TRANSPARENT)

                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val x = event.x
                        val y = event.y

                        currentExpandedPostIt?.let { postIt ->
                            // Verificar si el clic fue FUERA del PostIt expandido
                            if (isClickOutsidePostIt(x, y, postIt)) {
                                postIt.collapse()
                                return@setOnTouchListener true
                            }
                        }
                    }
                    false
                }
            }
        mainContainer.addView(postItOverlay)
    }

    private fun isClickOutsidePostIt(
        x: Float,
        y: Float,
        postIt: PostItView,
    ): Boolean {
        val postItLeft = postIt.x
        val postItTop = postIt.y
        val postItRight = postItLeft + postIt.width
        val postItBottom = postItTop + postIt.height

        return x < postItLeft || x > postItRight || y < postItTop || y > postItBottom
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createExpandedPizarra(postIt: PostItView) {
        expandedPizarraViewModel = PizarraViewModel()

        val postItX = postIt.x
        val postItY = postIt.y
        val postItWidth = postIt.width
        val postItHeight = postIt.height

        expandedPizarraView =
            PizarraView(requireContext()).apply {
                setModel(expandedPizarraViewModel!!)
                layoutParams =
                    FrameLayout.LayoutParams(
                        postItWidth,
                        (postItHeight - postIt.topBarHeight).toInt(),
                    )
                setBackgroundColor(Color.YELLOW)

                x = postItX
                y = postItY + postIt.topBarHeight

                setPadding(0, 0, 0, 0)
            }

        mainContainer.addView(expandedPizarraView)
        expandedPizarraView?.bringToFront()
        expandedPizarraView?.load()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun animateExpansion(postIt: PostItView) {
        val (originalX, originalY) = postIt.getOriginalPosition()
        val (originalWidth, originalHeight) = postIt.getOriginalSize()

        val displayMetrics = resources.displayMetrics
        val targetWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val targetHeight = (displayMetrics.heightPixels * 0.25).toInt()
        val targetX = (displayMetrics.widthPixels - targetWidth) / 2f
        val targetY = (displayMetrics.heightPixels - targetHeight) / 2f

        val animator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                addUpdateListener { animation ->
                    val fraction = animation.animatedValue as Float

                    val currentX = originalX + (targetX - originalX) * fraction
                    val currentY = originalY + (targetY - originalY) * fraction
                    val currentWidth = (originalWidth + (targetWidth - originalWidth) * fraction).toInt()
                    val currentHeight = (originalHeight + (targetHeight - originalHeight) * fraction).toInt()

                    postIt.x = currentX
                    postIt.y = currentY
                    postIt.layoutParams.width = currentWidth
                    postIt.layoutParams.height = currentHeight
                    postIt.requestLayout()

                    updateExpandedPizarraLayout(postIt, currentWidth, currentHeight)
                }
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            updateExpandedPizarraLayout(postIt, targetWidth, targetHeight)
                        }
                    },
                )
            }

        animator.start()
        postIt.bringToFront()
        animator.doOnEnd {
            createExpandedPizarra(postIt)
        }
    }

    private fun updateExpandedPizarraLayout(
        postIt: PostItView,
        currentWidth: Int,
        currentHeight: Int,
    ) {
        expandedPizarraView?.let { pizarra ->
            // Ajustar la PizarraView para que ocupe el área del PostIt (menos la barra superior)
            val topBarHeight = 60f * resources.displayMetrics.density
            pizarra.layoutParams =
                FrameLayout.LayoutParams(
                    currentWidth,
                    (currentHeight - topBarHeight).toInt(),
                )
            pizarra.x = postIt.x
            pizarra.y = postIt.y + topBarHeight
            pizarra.requestLayout()
        }
    }

    private fun collapsePostIt(postIt: PostItView) {
        captureAndScaleBitmap(postIt)

        // Animación de colapso
        animateCollapse(postIt)

        // Limpiar recursos
        cleanupExpansion()
    }

    private fun captureAndScaleBitmap(postIt: PostItView) {
        expandedPizarraView?.let { pizarra ->
            pizarra.post {
                try {
                    val originalBitmap = captureView(pizarra)
                    originalBitmap?.let { bitmap ->
                        // Usar un tamaño más pequeño para el preview
                        val scaledBitmap = scaleBitmapForPostIt(bitmap, postIt)
                        postIt.setPreview(scaledBitmap)
                        bitmap.recycle()

                        // Forzar redibujado
                        postIt.invalidate()
                    }
                } catch (e: Exception) {
                    Log.e("PostIt", "Error capturando bitmap: ${e.message}")
                }
            }
        }
    }

    private fun scaleBitmapForPostIt(
        originalBitmap: Bitmap,
        postIt: PostItView,
    ): Bitmap {
        val (originalWidth, originalHeight) = postIt.getOriginalSize()
        val topBarHeight = 60f * resources.displayMetrics.density

        // Área disponible para el preview (excluyendo barra superior y padding)
        val targetWidth = max(50, originalWidth - (closeButtonPadding * 2).toInt())
        val targetHeight = max(50, (originalHeight - topBarHeight - closeButtonPadding * 2).toInt())

        // Escalar manteniendo relación de aspecto
        val scale =
            min(
                targetWidth.toFloat() / originalBitmap.width,
                targetHeight.toFloat() / originalBitmap.height,
            )

        val scaledWidth = (originalBitmap.width * scale).toInt()
        val scaledHeight = (originalBitmap.height * scale).toInt()

        return originalBitmap.scale(scaledWidth, scaledHeight)
    }

    private fun captureView(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) return null

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun cleanupExpansion() {
        // Remover views en el orden correcto
        expandedPizarraView?.let {
            mainContainer.removeView(it)
            expandedPizarraView = null
        }

        postItOverlay?.let {
            mainContainer.removeView(it)
            postItOverlay = null
        }

        expandedPizarraViewModel = null
        currentExpandedPostIt = null
    }

    private fun animateCollapse(postIt: PostItView) {
        val (originalX, originalY) = postIt.getOriginalPosition()
        val (originalWidth, originalHeight) = postIt.getOriginalSize()

        val currentX = postIt.x
        val currentY = postIt.y
        val currentWidth = postIt.width
        val currentHeight = postIt.height

        val animator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                addUpdateListener { animation ->
                    val fraction = animation.animatedValue as Float

                    val newX = currentX + (originalX - currentX) * fraction
                    val newY = currentY + (originalY - currentY) * fraction
                    val newWidth = (currentWidth + (originalWidth - currentWidth) * fraction).toInt()
                    val newHeight = (currentHeight + (originalHeight - currentHeight) * fraction).toInt()

                    postIt.x = newX
                    postIt.y = newY
                    postIt.layoutParams.width = newWidth
                    postIt.layoutParams.height = newHeight
                    postIt.requestLayout()
                }
            }

        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupExpansion()
        drawView = null
    }
}
