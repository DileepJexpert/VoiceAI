package com.voiceai.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.model.Expense
import com.voiceai.app.domain.model.ScannedContact
import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.VoiceNote
import com.voiceai.app.domain.repository.ActionItemRepository
import com.voiceai.app.domain.repository.ContactRepository
import com.voiceai.app.domain.repository.ExpenseRepository
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter { ALL, VOICE_NOTES, SCANNED_DOCS, ACTION_ITEMS, CONTACTS, EXPENSES }

data class SearchUiState(
    val query: String = "",
    val voiceNoteResults: List<VoiceNote> = emptyList(),
    val scanResults: List<ScannedDocument> = emptyList(),
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(
                                voiceNoteResults = emptyList(),
                                scanResults = emptyList(),
                                isSearching = false
                            )
                        }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(isSearching = true) }

                    combine(
                        voiceNoteRepository.searchNotes(query),
                        scannedDocumentRepository.searchDocuments(query)
                    ) { notes, docs ->
                        Pair(notes, docs)
                    }.collectLatest { (notes, docs) ->
                        _uiState.update {
                            it.copy(
                                voiceNoteResults = notes,
                                scanResults = docs,
                                isSearching = false
                            )
                        }
                    }
                }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query
    }

    fun setFilter(filter: SearchFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun clearSearch() {
        _uiState.update {
            SearchUiState()
        }
        queryFlow.value = ""
    }
}
