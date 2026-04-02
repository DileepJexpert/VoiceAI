package com.voiceai.app.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfGenerator @Inject constructor(
    private val context: Context
) {
    companion object {
        // A4-ish dimensions in points (72 dpi): 595 x 842
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 36
    }

    /**
     * Generates a PDF file containing one page per image.
     *
     * @param imagePaths List of file paths to images to include.
     * @param title The document title (used as PDF metadata).
     * @param outputPath The output file path for the generated PDF.
     * @return The output file path.
     */
    fun generatePdf(imagePaths: List<String>, title: String, outputPath: String): String {
        val pdfDocument = PdfDocument()

        val contentWidth = PAGE_WIDTH - (MARGIN * 2)
        val contentHeight = PAGE_HEIGHT - (MARGIN * 2)

        imagePaths.forEachIndexed { index, imagePath ->
            val bitmap = BitmapFactory.decodeFile(imagePath) ?: return@forEachIndexed

            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Scale the image to fit within the content area while preserving aspect ratio
            val scaleX = contentWidth.toFloat() / bitmap.width
            val scaleY = contentHeight.toFloat() / bitmap.height
            val scale = minOf(scaleX, scaleY)

            val scaledWidth = (bitmap.width * scale).toInt()
            val scaledHeight = (bitmap.height * scale).toInt()

            // Center the image on the page
            val left = MARGIN + (contentWidth - scaledWidth) / 2
            val top = MARGIN + (contentHeight - scaledHeight) / 2

            val destRect = Rect(left, top, left + scaledWidth, top + scaledHeight)
            canvas.drawBitmap(bitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))

            pdfDocument.finishPage(page)
            bitmap.recycle()
        }

        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputPath
    }
}
