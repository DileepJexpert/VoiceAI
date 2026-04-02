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
    val actionItemResults: List<ActionItem> = emptyList(),
    val contactResults: List<ScannedContact> = emptyList(),
    val expenseResults: List<Expense> = emptyList(),
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val isSearching: Boolean = false,
    val aiAnswer: String? = null,
    val isAskingAI: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val actionItemRepository: ActionItemRepository,
    private val contactRepository: ContactRepository,
    private val expenseRepository: ExpenseRepository
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
                                actionItemResults = emptyList(),
                                contactResults = emptyList(),
                                expenseResults = emptyList(),
                                isSearching = false,
                                aiAnswer = null
                            )
                        }
                        return@collectLatest
                    }

                    _uiState.update { it.copy(isSearching = true) }

                    val lowerQuery = query.lowercase()

                    combine(
                        voiceNoteRepository.searchNotes(query),
                        scannedDocumentRepository.searchDocuments(query),
                        actionItemRepository.getAll(),
                        contactRepository.getAll(),
                        expenseRepository.getAll()
                    ) { notes, docs, allActions, allContacts, allExpenses ->
                        val filteredActions = allActions.filter {
                            it.title.lowercase().contains(lowerQuery)
                        }
                        val filteredContacts = allContacts.filter {
                            it.name.lowercase().contains(lowerQuery) ||
                                (it.company?.lowercase()?.contains(lowerQuery) == true)
                        }
                        val filteredExpenses = allExpenses.filter {
                            it.merchant.lowercase().contains(lowerQuery)
                        }
                        SearchResults(notes, docs, filteredActions, filteredContacts, filteredExpenses)
                    }.collectLatest { results ->
                        _uiState.update {
                            it.copy(
                                voiceNoteResults = results.notes,
                                scanResults = results.docs,
                                actionItemResults = results.actionItems,
                                contactResults = results.contacts,
                                expenseResults = results.expenses,
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

    fun askAI(question: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAskingAI = true, aiAnswer = null) }
            // Placeholder: in production this would call an AI service
            _uiState.update {
                it.copy(
                    isAskingAI = false,
                    aiAnswer = "AI analysis for \"$question\" is not yet available. Connect an AI backend to enable this feature."
                )
            }
        }
    }

    fun dismissAIAnswer() {
        _uiState.update { it.copy(aiAnswer = null) }
    }

    private data class SearchResults(
        val notes: List<VoiceNote>,
        val docs: List<ScannedDocument>,
        val actionItems: List<ActionItem>,
        val contacts: List<ScannedContact>,
        val expenses: List<Expense>
    )
}
