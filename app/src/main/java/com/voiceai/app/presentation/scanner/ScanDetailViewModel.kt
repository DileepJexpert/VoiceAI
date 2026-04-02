package com.voiceai.app.presentation.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.ScannedPage
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.TtsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanDetailUiState(
    val document: ScannedDocument? = null,
    val pages: List<ScannedPage> = emptyList(),
    val selectedTab: Int = 0,
    val isTtsSpeaking: Boolean = false,
    val ttsProgress: Float = 0f,
    val currentPageIndex: Int = 0,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val ttsSpeed: Float = 1.0f
)

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val ttsRepository: TtsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scanId: Long = savedStateHandle["scanId"] ?: 0L

    private val _uiState = MutableStateFlow(ScanDetailUiState())
    val uiState: StateFlow<ScanDetailUiState> = _uiState.asStateFlow()

    init {
        loadDocument()
        loadPages()
        observeTtsState()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val document = scannedDocumentRepository.getDocumentById(scanId)
            _uiState.update { it.copy(document = document, isLoading = false) }
        }
    }

    private fun loadPages() {
        viewModelScope.launch {
            scannedDocumentRepository.getPages(scanId).collect { pages ->
                _uiState.update { it.copy(pages = pages) }
            }
        }
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            ttsRepository.isPlaying().collect { isPlaying ->
                _uiState.update { it.copy(isTtsSpeaking = isPlaying) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            scannedDocumentRepository.toggleFavorite(scanId)
            val updatedDoc = scannedDocumentRepository.getDocumentById(scanId)
            _uiState.update { it.copy(document = updatedDoc) }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteDocument() {
        viewModelScope.launch {
            scannedDocumentRepository.deleteDocument(scanId)
            _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
        }
    }

    fun updateTitle(title: String) {
        val currentDoc = _uiState.value.document ?: return
        val updatedDoc = currentDoc.copy(title = title)
        _uiState.update { it.copy(document = updatedDoc) }
        viewModelScope.launch {
            scannedDocumentRepository.updateDocument(updatedDoc)
        }
    }

    fun startTts() {
        val text = _uiState.value.document?.extractedText ?: return
        viewModelScope.launch {
            ttsRepository.setSpeechRate(_uiState.value.ttsSpeed)
            ttsRepository.speak(text)
        }
    }

    fun stopTts() {
        viewModelScope.launch {
            ttsRepository.stop()
        }
    }

    fun toggleTts() {
        if (_uiState.value.isTtsSpeaking) stopTts() else startTts()
    }

    fun setTtsSpeed(speed: Float) {
        _uiState.update { it.copy(ttsSpeed = speed) }
        viewModelScope.launch {
            ttsRepository.setSpeechRate(speed)
        }
    }

    fun cycleTtsSpeed() {
        val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
        val currentIndex = speeds.indexOf(_uiState.value.ttsSpeed)
        val nextSpeed = speeds[(currentIndex + 1) % speeds.size]
        setTtsSpeed(nextSpeed)
    }

    fun exportPdf() {
        // PDF export integration would be wired here via PdfRepository
    }

    fun setCurrentPage(index: Int) {
        _uiState.update { it.copy(currentPageIndex = index) }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            ttsRepository.stop()
        }
    }
}
