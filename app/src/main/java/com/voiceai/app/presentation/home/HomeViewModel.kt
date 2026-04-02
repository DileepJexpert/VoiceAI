package com.voiceai.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.model.VoiceNote
import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.repository.ActionItemRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab { ALL, VOICE_NOTES, SCANNED_DOCS, ACTION_ITEMS }

data class HomeUiState(
    val voiceNotes: List<VoiceNote> = emptyList(),
    val scannedDocuments: List<ScannedDocument> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val selectedTab: HomeTab = HomeTab.ALL,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val actionItemRepository: ActionItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            voiceNoteRepository.getAllNotes().collect { notes ->
                _uiState.update { it.copy(voiceNotes = notes, isLoading = false) }
            }
        }
        viewModelScope.launch {
            scannedDocumentRepository.getAllDocuments().collect { docs ->
                _uiState.update { it.copy(scannedDocuments = docs, isLoading = false) }
            }
        }
        viewModelScope.launch {
            actionItemRepository.getPending().collect { items ->
                _uiState.update { it.copy(actionItems = items, isLoading = false) }
            }
        }
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteVoiceNote(id: Long) {
        viewModelScope.launch { voiceNoteRepository.deleteNote(id) }
    }

    fun deleteScannedDocument(id: Long) {
        viewModelScope.launch { scannedDocumentRepository.deleteDocument(id) }
    }

    fun toggleVoiceNoteFavorite(id: Long) {
        viewModelScope.launch { voiceNoteRepository.toggleFavorite(id) }
    }

    fun toggleScannedDocFavorite(id: Long) {
        viewModelScope.launch { scannedDocumentRepository.toggleFavorite(id) }
    }

    fun deleteActionItem(id: Long) {
        viewModelScope.launch { actionItemRepository.delete(id) }
    }

    fun updateActionItemStatus(id: Long, status: String) {
        viewModelScope.launch { actionItemRepository.updateStatus(id, status) }
    }
}
