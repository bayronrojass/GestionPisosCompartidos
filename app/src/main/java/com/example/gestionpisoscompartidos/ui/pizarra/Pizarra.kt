package com.example.gestionpisoscompartidos.ui.pizarra

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.R
import com.example.gestionpisoscompartidos.ui.pizarra.postit.PostItView
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.min
import com.example.gestionpisoscompartidos.data.remote.NetworkModule
import com.example.gestionpisoscompartidos.data.remote.RemoteRepository
import com.example.gestionpisoscompartidos.data.repository.APIs.CasaAPI
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
import com.example.gestionpisoscompartidos.utils.ApiResult
import java.time.Instant

open class Pizarra : Fragment() {
    private var casaId: Long = 0

    companion object {
        fun newInstance(casaId: Long) =
            Pizarra().apply {
                arguments =
                    Bundle().apply {
                        putLong("casa_id", casaId)
                    }
            }
    }

    private val repository = RemoteRepository(NetworkModule.retrofit.create(CasaAPI::class.java))

    private val viewModel: PizarraViewModel = PizarraViewModel(1)
    private var drawView: PizarraView? = null
    private lateinit var postItContainer: FrameLayout
    private lateinit var buttonContainer: LinearLayout
    private lateinit var mainContainer: ConstraintLayout

    private var currentExpandedPostIt: PostItView? = null
    private var postItOverlay: View? = null
    private var expandedPizarraView: PizarraView? = null
    private var expandedPizarraViewModel: PizarraViewModel? = null
    private val closeButtonPadding = 5f * Resources.getSystem().displayMetrics.density

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = PizarraArgs.fromBundle(requireArguments())
        casaId = args.casaId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        drawView?.setModel(viewModel)

        viewLifecycleOwner.lifecycleScope.launch {
            loadInitialData()

            drawView?.load()
            viewModel.bitmapState.collect { bitmap ->
                bitmap?.let {
                    drawView?.setBackgroundBitmap(it)
                }
            }
        }

        setupClickListeners(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadInitialData() {
        val lienzoResponse = repository.request { getLienzo(casaId) }

        when (lienzoResponse) {
            is ApiResult.Error -> {
                Log.e("Pizarra", "Error when loading lienzo " + lienzoResponse.message + lienzoResponse.code)
            }
            is ApiResult.Success<Long> -> {
                val lienzoId = lienzoResponse.data
                Log.d("Pizarra", "Lienzo encontrado: $lienzoId")
                lienzoId.let {
                    viewModel.lienzoId = it
                }
            }
            is ApiResult.Throws -> {
                Log.e("Pizarra", "Throws when loading lienzo " + lienzoResponse.exception.message)
            }
        }

        val postitResponse = repository.request { getPostIts(casaId) }

        when (postitResponse) {
            is ApiResult.Error -> {
                Log.e("Pizarra", "Error when loading postits " + postitResponse.message + postitResponse.code)
            }
            is ApiResult.Success<List<Long>> -> {
                val postItIds = postitResponse.data
                Log.d("Pizarra", "PostIts encontrados: $postItIds")
                postItIds.forEach { postItId ->
                    createExistingPostIt(postItId)
                }
            }
            is ApiResult.Throws -> {
                Log.e("Pizarra", "Throws when loading postIt " + postitResponse.exception.message)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createExistingPostIt(postItId: Long) {
        val postItResponse = repository.request { getPostItDetails(postItId) }

        when (postItResponse) {
            is ApiResult.Error -> {
                Log.e("Pizarra", "Error when loading postit " + postItResponse.message)
            }
            is ApiResult.Success<PostItDTO> -> {
                val details = postItResponse.data
                createPostItView(
                    id = postItId,
                    _lienzoId = details.lienzoId,
                    _x = details.posicionX,
                    _y = details.posicionY,
                    plegado = details.plegado,
                )
            }
            is ApiResult.Throws -> {
                Log.e("Pizarra", "Throws when loading postit " + postItResponse.exception.message)
            }
        }
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

        view.findViewById<Button>(R.id.btnUndo).setOnClickListener {
            addNewPostIt(100f, 100f)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addNewPostIt(
        _x: Float,
        _y: Float,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val request = repository.request { crearPostIt(casaId) }
            Log.d("Postit", "$request")
            when (request) {
                is ApiResult.Error -> {
                    Log.e("Postit", "Error creando postit " + request.message)
                }
                is ApiResult.Success<PostItDTO> -> {
                    val postIt =
                        PostItView(requireContext(), postItId = request.data.id).apply {
                            val layoutParams = FrameLayout.LayoutParams(300, 300)
                            this.layoutParams = layoutParams
                            x = _x
                            y = _y

                            onExpand = { expandedPostIt ->
                                expandPostIt(expandedPostIt, request.data.id)
                            }

                            onCollapse = { collapsedPostIt ->
                                collapsePostIt(collapsedPostIt)
                            }
                        }

                    postItContainer.addView(postIt)
                    postIt.post {
                        viewLifecycleOwner.lifecycleScope.launch {
                            Log.d("PostIt", "Loading...? ${postIt.model?.lienzoId}")
                            postIt.load()
                            postIt.invalidate()
                        }
                    }
                }
                is ApiResult.Throws -> {
                    Log.e("Postit", "Throws creando postit " + request.exception.message)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun expandPostIt(
        postIt: PostItView,
        lienzoId: Long,
    ) {
        if (currentExpandedPostIt != null) {
            return
        }

        currentExpandedPostIt = postIt

        createOverlay()

        animateExpansion(postIt, lienzoId)
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
                            if (isClickOutsidePostIt(x, y, postIt)) {
                                postIt.collapse()
                                return@setOnTouchListener true
                            } else if (isClickOutsidePostIt(x, y, postIt)) {
                                Log.d("Pizarra", "Ignorando click - postIt está animando")
                            }
                        }
                    }
                    false
                }
            }

        mainContainer.addView(postItOverlay)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPostItView(
        id: Long,
        _lienzoId: Long,
        _x: Float,
        _y: Float,
        plegado: Boolean,
    ) {
        val postIt =
            PostItView(requireContext(), postItId = id).apply {
                this.id = id.toInt()
                val layoutParams = FrameLayout.LayoutParams(300, 300)
                this.layoutParams = layoutParams
                x = _x
                y = _y
                // TODO
                // isExpanded = !plegado
                model =
                    PizarraViewModel(_lienzoId).apply {
                        color = viewModel.color
                        lienzoId = _lienzoId
                    }

                onExpand = { expandedPostIt ->
                    expandPostIt(expandedPostIt, _lienzoId)
                }

                onCollapse = { collapsedPostIt ->
                    collapsePostIt(collapsedPostIt)
                }
            }

        postItContainer.addView(postIt)

        postIt.post {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("PostIt", "Loading...? ${postIt.model?.lienzoId}")
                postIt.load()
                postIt.invalidate()
            }
        }
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
    private fun createExpandedPizarra(
        postIt: PostItView,
        lienzoId: Long,
    ) {
        expandedPizarraViewModel = postIt.model ?: PizarraViewModel(lienzoId).apply {
            color = viewModel.color
        }

        if (postIt.model == null) {
            postIt.model = expandedPizarraViewModel
            postIt.model!!.color = viewModel.color
        }

        expandedPizarraViewModel?.lienzoId = lienzoId
        expandedPizarraViewModel?.lastLoaded = Instant.ofEpochMilli(10000)

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

        viewLifecycleOwner.lifecycleScope.launch {
            expandedPizarraViewModel!!.bitmapState.collect { bitmap ->
                bitmap?.let {
                    expandedPizarraView?.setBackgroundBitmap(it)
                    expandedPizarraView?.invalidate()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("Pizarra", "Cargando pizarra expandida con lienzoId: $lienzoId")
            expandedPizarraViewModel!!.load()
        }

        postIt.bringToFront()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun animateExpansion(
        postIt: PostItView,
        lienzoId: Long,
    ) {
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
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            Log.d("Pizarra", "Animación de expansión iniciada")
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            Log.d("Pizarra", "Animación de expansión finalizada")
                            try {
                                updateExpandedPizarraLayout(postIt, targetWidth, targetHeight)
                                createExpandedPizarra(postIt, lienzoId)
                            } catch (e: Exception) {
                                Log.e("Pizarra", "Error en animación de expansión: ${e.message}")
                            }
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            Log.d("Pizarra", "Animación de expansión cancelada")
                        }
                    },
                )
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
            }

        animator.start()
        postIt.bringToFront()
    }

    private fun updateExpandedPizarraLayout(
        postIt: PostItView,
        currentWidth: Int,
        currentHeight: Int,
    ) {
        expandedPizarraView?.let { pizarra ->
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
        postIt.model?.color = viewModel.color
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collapsePostIt(postIt: PostItView) {
        Log.d("Pizarra", "Intentando colapsar post-it ${postIt.postItId}")

        try {
            captureAndScaleBitmap(postIt)

            expandedPizarraView?.stop()

            animateCollapse(postIt)
            cleanupExpansion()

            // postIt.postDelayed({
            //    viewLifecycleOwner.lifecycleScope.launch {
            //        Log.d("Pizarra", "Reiniciando carga después del colapso")
            //        postIt.load()
            //    }
            // }, 500L)
        } catch (e: Exception) {
            Log.e("Pizarra", "Error durante el colapso: ${e.message}")
        }
    }

    private fun captureAndScaleBitmap(postIt: PostItView) {
        expandedPizarraView?.let { pizarra ->
            pizarra.post {
                try {
                    val fullBitmap = captureView(pizarra)
                    if (fullBitmap != null) {
                        val croppedBitmap = cropVisibleArea(pizarra, fullBitmap)
                        val scaledBitmap = scaleBitmapForPreview(croppedBitmap, postIt)

                        postIt.setPreview(scaledBitmap)
                        postIt.view?.setBackgroundBitmap(scaledBitmap)
                        postIt.invalidate()
                    } else {
                        Log.e("PostIt", "No se pudo capturar el bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("PostIt", "Error capturando bitmap: ${e.message}")
                }
            }
        } ?: Log.e("PostIt", "No hay pizarra expandida para capturar")
    }

    private fun cropVisibleArea(
        view: View,
        bitmap: Bitmap,
    ): Bitmap {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)

        val visibleLeft = max(0, rect.left - view.left)
        val visibleTop = max(0, rect.top - view.top)
        val visibleWidth = min(bitmap.width - visibleLeft, rect.width())
        val visibleHeight = min(bitmap.height - visibleTop, rect.height())

        if (visibleWidth <= 0 || visibleHeight <= 0) {
            Log.w("PostIt", "No hay área visible para recortar")
            return bitmap
        }

        return Bitmap.createBitmap(bitmap, visibleLeft, visibleTop, visibleWidth, visibleHeight)
    }

    private fun scaleBitmapForPreview(
        original: Bitmap,
        postIt: PostItView,
    ): Bitmap {
        val (targetWidth, targetHeight) = postIt.getOriginalSize()

        val ratio =
            min(
                targetWidth.toFloat() / original.width,
                targetHeight.toFloat() / original.height,
            )

        val scaledWidth = (original.width * ratio).toInt()
        val scaledHeight = (original.height * ratio).toInt()

        return Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true)
    }

    private fun captureView(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) return null

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun cleanupExpansion() {
        expandedPizarraView?.let {
            mainContainer.removeView(it)
            expandedPizarraView = null
        }

        postItOverlay?.let {
            mainContainer.removeView(it)
            postItOverlay = null
        }

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
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            Log.d("Pizarra", "Animación de colapso iniciada")
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            Log.d("Pizarra", "Animación de colapso finalizada")
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            Log.d("Pizarra", "Animación de colapso cancelada")
                        }
                    },
                )

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
        drawView!!.stop()
        drawView = null
    }
}
