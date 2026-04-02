package com.voiceai.app.presentation.actionitems

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.usecase.ManageActionItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PriorityFilter { ALL, HIGH, MEDIUM, LOW }

data class ActionItemsUiState(
    val todoItems: List<ActionItem> = emptyList(),
    val inProgressItems: List<ActionItem> = emptyList(),
    val doneItems: List<ActionItem> = emptyList(),
    val filter: PriorityFilter = PriorityFilter.ALL,
    val selectedTab: Int = 0,
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class ActionItemsViewModel @Inject constructor(
    private val manageActionItemsUseCase: ManageActionItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActionItemsUiState())
    val uiState: StateFlow<ActionItemsUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            manageActionItemsUseCase.getByStatus("todo").collect { items ->
                _uiState.update { it.copy(todoItems = applyFilter(items), isLoading = false) }
            }
        }
        viewModelScope.launch {
            manageActionItemsUseCase.getByStatus("in_progress").collect { items ->
                _uiState.update { it.copy(inProgressItems = applyFilter(items), isLoading = false) }
            }
        }
        viewModelScope.launch {
            manageActionItemsUseCase.getByStatus("done").collect { items ->
                _uiState.update { it.copy(doneItems = applyFilter(items), isLoading = false) }
            }
        }
    }

    private fun applyFilter(items: List<ActionItem>): List<ActionItem> {
        return when (_uiState.value.filter) {
            PriorityFilter.ALL -> items
            PriorityFilter.HIGH -> items.filter { it.priority == 3 }
            PriorityFilter.MEDIUM -> items.filter { it.priority == 2 }
            PriorityFilter.LOW -> items.filter { it.priority == 1 }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setFilter(filter: PriorityFilter) {
        _uiState.update { it.copy(filter = filter) }
        loadItems()
    }

    fun updateStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            manageActionItemsUseCase.updateStatus(id, newStatus)
        }
    }

    fun addItem(title: String, priority: Int, dueDate: Long?) {
        viewModelScope.launch {
            val item = ActionItem(
                id = 0,
                title = title,
                sourceNoteId = null,
                sourceScanId = null,
                status = "todo",
                priority = priority,
                dueDate = dueDate,
                createdAt = System.currentTimeMillis(),
                completedAt = null
            )
            manageActionItemsUseCase.insert(item)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            manageActionItemsUseCase.delete(id)
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }
}
