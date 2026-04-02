package com.voiceai.app.presentation.scanner

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.ScannedPage
import com.voiceai.app.domain.repository.AISummaryRepository
import com.voiceai.app.domain.repository.OcrRepository
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.util.Constants
import com.voiceai.app.util.DocumentScanner
import com.voiceai.app.util.FileUtils
import com.voiceai.app.util.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CapturedPage(
    val imagePath: String,
    val rawImagePath: String,
    val processedBitmap: Bitmap? = null,
    val text: String? = null,
    val filter: String = "ORIGINAL"
)

enum class ScanStep {
    CAMERA, CROP, REVIEW, PROCESSING
}

data class ScannerUiState(
    val capturedPages: List<CapturedPage> = emptyList(),
    val currentStep: ScanStep = ScanStep.CAMERA,
    val isProcessing: Boolean = false,
    val processingStep: String = "",
    val detectedCorners: List<Offset>? = null,
    val selectedFilter: String = "ORIGINAL",
    val savedDocumentId: Long? = null,
    val flashEnabled: Boolean = false,
    val autoCaptureEnabled: Boolean = true
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val ocrRepository: OcrRepository,
    private val aiSummaryRepository: AISummaryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val documentScanner = DocumentScanner()

    private var currentRawImagePath: String? = null

    fun onImageCaptured(imagePath: String) {
        currentRawImagePath = imagePath
        val bitmap = ImageProcessor.loadBitmapFromFile(imagePath) ?: return
        val edges = documentScanner.detectEdges(bitmap)
        val corners = edges.map { Offset(it.x, it.y) }
        _uiState.update {
            it.copy(
                detectedCorners = corners,
                currentStep = ScanStep.CROP
            )
        }
    }

    fun onCropConfirmed(croppedBitmap: Bitmap) {
        val rawPath = currentRawImagePath ?: return
        val filter = _uiState.value.selectedFilter
        val filteredBitmap = documentScanner.applyFilter(croppedBitmap, filter)

        val fileName = FileUtils.generateFileName("scan_page", Constants.IMAGE_FORMAT)
        val processedPath = FileUtils.getScanFilePath(context, fileName)
        ImageProcessor.saveBitmapToFile(filteredBitmap, processedPath)

        val page = CapturedPage(
            imagePath = processedPath,
            rawImagePath = rawPath,
            processedBitmap = filteredBitmap,
            filter = filter
        )

        _uiState.update {
            it.copy(
                capturedPages = it.capturedPages + page,
                currentStep = ScanStep.REVIEW,
                selectedFilter = "ORIGINAL"
            )
        }
        currentRawImagePath = null
    }

    fun addPage() {
        _uiState.update { it.copy(currentStep = ScanStep.CAMERA) }
    }

    fun removePage(index: Int) {
        _uiState.update {
            val updated = it.capturedPages.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            it.copy(capturedPages = updated)
        }
    }

    fun reorderPages(from: Int, to: Int) {
        _uiState.update {
            val pages = it.capturedPages.toMutableList()
            if (from in pages.indices && to in pages.indices) {
                val item = pages.removeAt(from)
                pages.add(to, item)
            }
            it.copy(capturedPages = pages)
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(flashEnabled = !it.flashEnabled) }
    }

    fun toggleAutoCapture() {
        _uiState.update { it.copy(autoCaptureEnabled = !it.autoCaptureEnabled) }
    }

    fun getCurrentRawImagePath(): String? = currentRawImagePath

    fun goBackToCamera() {
        currentRawImagePath = null
        _uiState.update {
            it.copy(
                currentStep = ScanStep.CAMERA,
                detectedCorners = null
            )
        }
    }

    fun updateCorners(corners: List<Offset>) {
        _uiState.update { it.copy(detectedCorners = corners) }
    }

    fun processAndSave(title: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingStep = "Enhancing images...",
                    currentStep = ScanStep.PROCESSING
                )
            }

            try {
                // Step 1: Enhance images
                val enhancedPages = _uiState.value.capturedPages.map { page ->
                    val bitmap = page.processedBitmap
                        ?: ImageProcessor.loadBitmapFromFile(page.imagePath)
                        ?: return@map page
                    val enhanced = ImageProcessor.adjustBrightness(bitmap, 10f)
                    val enhancedPath = page.imagePath
                    ImageProcessor.saveBitmapToFile(enhanced, enhancedPath)
                    page.copy(processedBitmap = enhanced)
                }

                // Step 2: OCR
                _uiState.update { it.copy(processingStep = "Extracting text (OCR)...") }
                val ocrPages = enhancedPages.map { page ->
                    val text = try {
                        ocrRepository.extractText(page.imagePath)
                    } catch (_: Exception) {
                        ""
                    }
                    page.copy(text = text)
                }

                val fullText = ocrPages
                    .mapNotNull { it.text }
                    .filter { it.isNotBlank() }
                    .joinToString("\n\n---\n\n")

                // Step 3: AI Summary
                _uiState.update { it.copy(processingStep = "Generating AI summary...") }
                val summary = if (fullText.isNotBlank()) {
                    try {
                        aiSummaryRepository.summarizeDocument(fullText)
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }

                // Step 4: Save to database
                val now = System.currentTimeMillis()
                val document = ScannedDocument(
                    id = 0L,
                    title = title.ifBlank { "Scan ${FileUtils.generateFileName("", "").trimStart('_')} " },
                    extractedText = fullText.ifBlank { null },
                    summary = summary,
                    pageCount = ocrPages.size,
                    createdAt = now,
                    updatedAt = now,
                    language = Constants.DEFAULT_LANGUAGE,
                    isFavorite = false,
                    folderId = null,
                    pdfFilePath = null
                )

                val documentId = scannedDocumentRepository.insertDocument(document)

                ocrPages.forEachIndexed { index, page ->
                    val scannedPage = ScannedPage(
                        id = 0L,
                        documentId = documentId,
                        pageNumber = index + 1,
                        imagePath = page.imagePath,
                        rawImagePath = page.rawImagePath,
                        pageText = page.text,
                        filter = page.filter
                    )
                    scannedDocumentRepository.insertPage(scannedPage)
                }

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingStep = "",
                        savedDocumentId = documentId
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingStep = "Error: ${e.message}",
                        currentStep = ScanStep.REVIEW
                    )
                }
            }
        }
    }
}
