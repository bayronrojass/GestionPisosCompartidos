package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color

class PostItAnimationManager(
    private val context: Context,
    private val postItManager: PostItManager,
) {
    private var currentAnimator: Animator? = null

    fun animateExpansion(
        postIt: PostItView,
        lienzoId: Long,
    ) {
        currentAnimator?.cancel()

        val (originalX, originalY) = postIt.getOriginalPosition()
        val (originalWidth, originalHeight) = postIt.getOriginalSize()

        val displayMetrics = context.resources.displayMetrics
        val targetWidth = (displayMetrics.widthPixels * 0.6).toInt()
        val targetHeight = (displayMetrics.heightPixels * 0.25).toInt()
        val targetX = (displayMetrics.widthPixels - targetWidth) / 2f
        val targetY = (displayMetrics.heightPixels - targetHeight) / 2f

        currentAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                setupExpansionListeners(postIt, lienzoId)
                setupExpansionUpdateListener(
                    postIt,
                    originalX,
                    originalY,
                    originalWidth,
                    originalHeight,
                    targetX,
                    targetY,
                    targetWidth,
                    targetHeight,
                )
                start()
            }
    }

    fun animateCollapse(postIt: PostItView) {
        currentAnimator?.cancel()

        val (originalX, originalY) = postIt.getOriginalPosition()
        val (originalWidth, originalHeight) = postIt.getOriginalSize()

        val currentX = postIt.x
        val currentY = postIt.y
        val currentWidth = postIt.width
        val currentHeight = postIt.height

        currentAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 300
                setupCollapseListeners(postIt)
                setupCollapseUpdateListener(
                    postIt,
                    currentX,
                    currentY,
                    currentWidth,
                    currentHeight,
                    originalX,
                    originalY,
                    originalWidth,
                    originalHeight,
                )
                start()
            }
    }

    private fun ValueAnimator.setupExpansionListeners(
        postIt: PostItView,
        lienzoId: Long,
    ) {
        addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    postItManager.updateExpandedPizarraLayout(postIt)
                    postItManager.createExpandedPizarra(postIt, lienzoId)
                    postIt.isExpansionInProgress = false
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    postIt.isExpansionInProgress = false
                    currentAnimator = null
                }
            },
        )
    }

    private fun ValueAnimator.setupCollapseListeners(postIt: PostItView) {
        addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                }

                override fun onAnimationEnd(animation: Animator) {
                    postItManager.cleanupExpansion()
                    postIt.isCollapseInProgress = false
                    currentAnimator = null

                    postIt.post {
                        postIt.requestLayout()
                        postIt.invalidate()
                        postIt.setBackgroundColor(Color.TRANSPARENT)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    postIt.isCollapseInProgress = false
                    currentAnimator = null
                }
            },
        )
    }

    private fun ValueAnimator.setupExpansionUpdateListener(
        postIt: PostItView,
        originalX: Float,
        originalY: Float,
        originalWidth: Int,
        originalHeight: Int,
        targetX: Float,
        targetY: Float,
        targetWidth: Int,
        targetHeight: Int,
    ) {
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

            postItManager.updateExpandedPizarraLayout(postIt, currentWidth, currentHeight)
        }
    }

    private fun ValueAnimator.setupCollapseUpdateListener(
        postIt: PostItView,
        currentX: Float,
        currentY: Float,
        currentWidth: Int,
        currentHeight: Int,
        originalX: Float,
        originalY: Float,
        originalWidth: Int,
        originalHeight: Int,
    ) {
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

    fun cleanup() {
        currentAnimator?.cancel()
        currentAnimator = null
    }
}
