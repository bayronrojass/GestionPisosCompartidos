package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.graphics.createBitmap
import com.example.gestionpisoscompartidos.ui.pizarra.PizarraView
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.scale

class BitmapManager {
    fun captureAndScalePreview(
        bitmap: Bitmap,
        postIt: PostItView,
    ) {
        try {
            val scaledBitmap = scaleBitmapForPreview(bitmap, postIt)

            postIt.setPreview(scaledBitmap)
        } catch (e: Exception) {
            Log.e("PostIt", "Error capturando bitmap: ${e.message}")
        }
    }

    fun captureAndScaleBitmap(
        postIt: PostItView,
        expandedPizarraView: PizarraView,
    ) {
        expandedPizarraView.let { pizarra ->
            pizarra.post {
                try {
                    val fullBitmap = captureView(pizarra)
                    if (fullBitmap != null) {
                        val croppedBitmap = cropVisibleArea(pizarra, fullBitmap)
                        val scaledBitmap = scaleBitmapForPreview(croppedBitmap, postIt)

                        postIt.setPreview(scaledBitmap)
                    } else {
                        Log.e("PostIt", "No se pudo capturar el bitmap")
                    }
                } catch (e: Exception) {
                    Log.e("PostIt", "Error capturando bitmap: ${e.message}")
                }
            }
        }
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

        return original.scale(scaledWidth, scaledHeight)
    }

    private fun captureView(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) return null

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
