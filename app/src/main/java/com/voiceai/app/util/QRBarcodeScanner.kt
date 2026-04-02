package com.voiceai.app.util

import android.content.Context
import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class QRResult(
    val content: String,
    val type: String,
    val format: String
)

@Singleton
class QRBarcodeScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun processImage(imagePath: String): List<QRResult> {
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return emptyList()
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()

        return try {
            val barcodes = scanner.process(inputImage).await()
            barcodes.mapNotNull { barcode ->
                val content = barcode.rawValue ?: return@mapNotNull null
                val type = if (content.startsWith("upi://", ignoreCase = true)) {
                    "upi"
                } else {
                    parseValueType(barcode.valueType)
                }
                QRResult(
                    content = content,
                    type = type,
                    format = parseFormat(barcode.format)
                )
            }
        } finally {
            scanner.close()
        }
    }

    private fun parseValueType(valueType: Int): String {
        return when (valueType) {
            Barcode.TYPE_URL -> "url"
            Barcode.TYPE_WIFI -> "wifi"
            Barcode.TYPE_CONTACT_INFO -> "contact"
            Barcode.TYPE_EMAIL -> "email"
            Barcode.TYPE_PHONE -> "phone"
            Barcode.TYPE_SMS -> "text"
            Barcode.TYPE_GEO -> "text"
            Barcode.TYPE_CALENDAR_EVENT -> "text"
            Barcode.TYPE_DRIVER_LICENSE -> "text"
            Barcode.TYPE_ISBN -> "text"
            Barcode.TYPE_PRODUCT -> "text"
            Barcode.TYPE_TEXT -> "text"
            Barcode.TYPE_UNKNOWN -> "text"
            else -> "text"
        }
    }

    private fun parseFormat(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            Barcode.FORMAT_AZTEC -> "AZTEC"
            Barcode.FORMAT_CODABAR -> "CODABAR"
            Barcode.FORMAT_CODE_39 -> "CODE_39"
            Barcode.FORMAT_CODE_93 -> "CODE_93"
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
            Barcode.FORMAT_EAN_8 -> "EAN_8"
            Barcode.FORMAT_EAN_13 -> "EAN_13"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_PDF417 -> "PDF417"
            Barcode.FORMAT_UPC_A -> "UPC_A"
            Barcode.FORMAT_UPC_E -> "UPC_E"
            else -> "UNKNOWN"
        }
    }
}
