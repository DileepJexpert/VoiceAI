package com.voiceai.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

/**
 * Image processing utilities for bitmap manipulation.
 */
object ImageProcessor {

    /**
     * Rotates a bitmap by the given number of degrees.
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Converts a bitmap to grayscale.
     */
    fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val grayscale = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return grayscale
    }

    /**
     * Adjusts the brightness of a bitmap.
     *
     * @param value Brightness adjustment value. 0 = no change, positive = brighter, negative = darker.
     *              Typical range is -255 to 255.
     */
    fun adjustBrightness(bitmap: Bitmap, value: Float): Bitmap {
        val adjusted = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(adjusted)
        val paint = Paint()
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return adjusted
    }

    /**
     * Saves a bitmap to a file at the given path.
     *
     * @param bitmap The bitmap to save.
     * @param filePath The output file path.
     * @param quality Compression quality (0-100), default 90.
     * @return The file path where the bitmap was saved.
     */
    fun saveBitmapToFile(bitmap: Bitmap, filePath: String, quality: Int = 90): String {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { outputStream ->
            val format = if (filePath.endsWith(".png", ignoreCase = true)) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, quality, outputStream)
            outputStream.flush()
        }
        return filePath
    }

    /**
     * Loads a bitmap from a file. Returns null if the file does not exist
     * or cannot be decoded.
     */
    fun loadBitmapFromFile(filePath: String): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null
        return BitmapFactory.decodeFile(filePath)
    }

    /**
     * Resizes a bitmap to fit within the given maximum dimensions while
     * preserving the aspect ratio.
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratioWidth = maxWidth.toFloat() / width
        val ratioHeight = maxHeight.toFloat() / height
        val scale = minOf(ratioWidth, ratioHeight)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
