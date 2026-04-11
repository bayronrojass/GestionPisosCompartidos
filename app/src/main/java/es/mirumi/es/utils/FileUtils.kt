import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun uriToFile(
    context: Context,
    uri: Uri,
): File? {
    val tempFile = File(context.cacheDir, "temp_profile_img_${System.currentTimeMillis()}.jpg")

    var rotationDegrees = 0f
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val exif = ExifInterface(inputStream)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        rotationDegrees =
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
    }

    val bitmap =
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: return null

    val maxSize = 800
    var width = bitmap.width
    var height = bitmap.height
    val bitmapRatio = width.toFloat() / height.toFloat()

    if (width > maxSize || height > maxSize) {
        if (width > height) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
    }
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

    val matrix = Matrix()
    if (rotationDegrees != 0f) {
        matrix.postRotate(rotationDegrees)
    }
    val finalBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)

    FileOutputStream(tempFile).use { outputStream ->
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    }

    return tempFile
}
