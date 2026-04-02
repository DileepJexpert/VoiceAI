package com.voiceai.app.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import com.voiceai.app.domain.model.ScannedPage
import com.voiceai.app.domain.repository.PdfRepository
import com.voiceai.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PdfRepository {

    override suspend fun generatePdf(pages: List<ScannedPage>, title: String): String =
        withContext(Dispatchers.IO) {
            val pdfDir = File(context.filesDir, Constants.PDFS_DIR)
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            val fileName = "${sanitizedTitle}_${System.currentTimeMillis()}${Constants.PDF_FORMAT}"
            val outputFile = File(pdfDir, fileName)

            val pdfDocument = PdfDocument()

            try {
                pages.forEachIndexed { index, page ->
                    val bitmap = BitmapFactory.decodeFile(page.imagePath)
                        ?: throw IllegalArgumentException(
                            "Could not decode image at path: ${page.imagePath}"
                        )

                    val pageInfo = PdfDocument.PageInfo.Builder(
                        bitmap.width,
                        bitmap.height,
                        index + 1
                    ).create()

                    val pdfPage = pdfDocument.startPage(pageInfo)
                    pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)

                    bitmap.recycle()
                }

                FileOutputStream(outputFile).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            } finally {
                pdfDocument.close()
            }

            outputFile.absolutePath
        }
}
