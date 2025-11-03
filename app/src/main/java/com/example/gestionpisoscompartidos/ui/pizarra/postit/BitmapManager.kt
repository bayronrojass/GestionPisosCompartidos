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

class BitmapManager {
    fun captureAndScalePreview(
        bitmap: Bitmap,
        drawRect: Rect,
    ): Bitmap {
        try {
            val targetWidth = drawRect.width()
            val targetHeight = drawRect.height()

            Log.d("BitmapManager", "Escalando para rect: ${targetWidth}x$targetHeight")

            return scaleBitmapForPreview(bitmap, targetWidth, targetHeight)
        } catch (e: Exception) {
            Log.e("BitmapManager", "Error en captureAndScalePreview: ${e.message}")
            return bitmap
        }
    }

    private fun scaleBitmapForPreview(
        original: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap {
        if (original.width <= 0 || original.height <= 0) {
            return original
        }

        if (targetWidth <= 0 || targetHeight <= 0) {
            return original
        }

        val ratio =
            min(
                targetWidth.toFloat() / original.width,
                targetHeight.toFloat() / original.height,
            )

        val scaledWidth = (original.width * ratio).toInt()
        val scaledHeight = (original.height * ratio).toInt()

        return Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true)
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
                        val scaledBitmap = scaleBitmapForPreview(croppedBitmap, postIt.width, postIt.height)

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

    private fun captureView(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) return null

        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
