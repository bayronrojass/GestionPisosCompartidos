package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.gestionpisoscompartidos.data.repository.repositories.RepositoryPostIt
import com.example.gestionpisoscompartidos.model.dtos.PostItDTO
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraView
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraViewModel
import kotlinx.coroutines.launch
import java.time.Instant

class PostItManager(
    private val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val postItContainer: ViewGroup,
    private val mainContainer: ViewGroup,
    private val viewModel: PizarraViewModel,
) {
    private var currentExpandedPostIt: PostItView? = null
    private var postItOverlay: View? = null
    private var expandedPizarraView: PizarraView? = null
    private var expandedPizarraViewModel: PizarraViewModel? = null
    private val repositoryPostIt = RepositoryPostIt()
    private val bitmapManager = BitmapManager()
    private val animationManager = PostItAnimationManager(context, this)

    fun createNewPostIt(
        dto: PostItDTO,
        x: Float,
        y: Float,
    ) {
        Log.d("Pizarra", "${dto.lienzoId}")
        val postIt =
            PostItView(context, postItId = dto.id).apply {
                setupLayout(PostItConfig().width, PostItConfig().height, x, y)
                this.onExpand = { onPostItExpand(this, dto.lienzoId) }
                this.onCollapse = { onPostItCollapse(this) }
            }

        addPostItToContainer(postIt)
        loadPostItContent(postIt)
    }

    suspend fun createExistingPostIt(postItId: Long) {
        val postItResponse = repositoryPostIt.getPostItDetails(postItId)
        if (postItResponse != null) {
            createPostItFromDTO(postItResponse)
        }
    }

    private fun createPostItFromDTO(details: PostItDTO) {
        val postIt =
            PostItView(context, postItId = details.id).apply {
                setupLayout(PostItConfig().width, PostItConfig().height, details.posicionX, details.posicionY)
                isContentVisible = !details.plegado
                model = createPizarraViewModel(details.lienzoId)
                this.onExpand = { onPostItExpand(this, details.lienzoId) }
                this.onCollapse = { onPostItCollapse(this) }
            }

        addPostItToContainer(postIt)
        loadPostItContent(postIt)
    }

    private fun createPizarraViewModel(lienzoId: Long): PizarraViewModel =
        PizarraViewModel(lienzoId).apply {
            color = viewModel.color
            this.lienzoId = lienzoId
        }

    private fun addPostItToContainer(postIt: PostItView) {
        postItContainer.addView(postIt)
    }

    private fun loadPostItContent(postIt: PostItView) {
        postIt.post {
            viewLifecycleOwner.lifecycleScope.launch {
                Log.d("PostIt", "Loading...? ${postIt.model?.lienzoId}")
                postIt.load()
                postIt.invalidate()
            }
        }
    }

    fun onPostItExpand(
        postIt: PostItView,
        lienzoId: Long,
    ) {
        if (currentExpandedPostIt != null || postIt.isExpansionInProgress) return

        postIt.isExpansionInProgress = true
        currentExpandedPostIt = postIt

        animationManager.animateExpansion(postIt, lienzoId)
        createOverlay()
    }

    fun onPostItCollapse(postIt: PostItView) {
        if (postIt.isCollapseInProgress) return
        postIt.isCollapseInProgress = true

        captureAndScaleBitmap(postIt)
        expandedPizarraView?.stop()

        animationManager.animateCollapse(postIt)
        cleanupExpansion()
    }

    private fun createOverlay() {
        postItOverlay =
            View(context).apply {
                layoutParams =
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                setBackgroundColor(Color.TRANSPARENT)

                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        currentExpandedPostIt?.let { postIt ->
                            if (postIt.isExpansionInProgress || postIt.isCollapseInProgress) {
                                return@setOnTouchListener true
                            }

                            if (isClickOutsidePostIt(event.x, event.y, postIt)) {
                                postIt.collapse()
                                return@setOnTouchListener true
                            }
                        }
                        performClick()
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

    fun createExpandedPizarra(
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
            PizarraView(context).apply {
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

    private fun captureAndScaleBitmap(postIt: PostItView) {
        expandedPizarraView?.let { pizarra ->
            pizarra.post {
                bitmapManager.captureAndScaleBitmap(postIt, pizarra)
            }
        } ?: Log.e("PostIt", "No hay pizarra expandida para capturar")
    }

    fun updateExpandedPizarraLayout(
        postIt: PostItView,
        currentWidth: Int? = null,
        currentHeight: Int? = null,
    ) {
        expandedPizarraView?.let { pizarra ->
            val topBarHeight = 60f * context.resources.displayMetrics.density
            val width = currentWidth ?: postIt.width
            val height = currentHeight ?: postIt.height

            pizarra.layoutParams =
                FrameLayout.LayoutParams(
                    width,
                    (height - topBarHeight).toInt(),
                )
            pizarra.x = postIt.x
            pizarra.y = postIt.y + topBarHeight
            pizarra.requestLayout()
        }
        postIt.model?.color = viewModel.color
    }

    fun getExpandedPizarraView(): PizarraView? = expandedPizarraView

    private fun stopExpandedPizarra() {
        expandedPizarraView?.stop()
    }

    fun cleanupExpansion() {
        expandedPizarraView?.let {
            it.stop()
            mainContainer.removeView(it)
            expandedPizarraView = null
        }

        postItOverlay?.let {
            mainContainer.removeView(it)
            postItOverlay = null
        }
        expandedPizarraViewModel = null

        currentExpandedPostIt?.isExpansionInProgress = false
        currentExpandedPostIt = null
    }

    fun cleanup() {
        cleanupExpansion()
        animationManager.cleanup()
        stopExpandedPizarra()
    }
}
