package com.voiceai.app.presentation.qrscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.QRScanResult
import com.voiceai.app.domain.repository.QRScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QRScannerUiState(
    val scanResult: QRScanResult? = null,
    val history: List<QRScanResult> = emptyList(),
    val showHistory: Boolean = false,
    val selectedTab: Int = 0
)

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val qrScanRepository: QRScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRScannerUiState())
    val uiState: StateFlow<QRScannerUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun onQRDetected(content: String, type: String) {
        val result = QRScanResult(
            id = 0L,
            content = content,
            type = type,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            val id = qrScanRepository.insert(result)
            val saved = result.copy(id = id)
            _uiState.update { it.copy(scanResult = saved) }
            loadHistory()
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(scanResult = null) }
    }

    fun loadHistory() {
        viewModelScope.launch {
            qrScanRepository.getAll().collect { results ->
                _uiState.update { it.copy(history = results) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            qrScanRepository.delete(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            qrScanRepository.clearAll()
        }
    }

    companion object {
        fun detectQRType(content: String): String {
            return when {
                content.startsWith("http://") || content.startsWith("https://") -> "URL"
                content.startsWith("WIFI:") -> "WiFi"
                content.startsWith("BEGIN:VCARD") -> "Contact"
                content.startsWith("upi://") -> "UPI"
                content.startsWith("tel:") -> "Phone"
                content.startsWith("mailto:") -> "Email"
                content.startsWith("geo:") -> "Location"
                content.startsWith("SMSTO:") || content.startsWith("sms:") -> "SMS"
                else -> "Text"
            }
        }
    }
}
