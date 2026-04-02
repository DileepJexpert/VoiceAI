package com.voiceai.app.presentation.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.Expense
import com.voiceai.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReceiptDetailUiState(
    val expense: Expense? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showDatePicker: Boolean = false
)

val ExpenseCategories = listOf(
    "Food", "Travel", "Office", "Shopping", "Utilities", "Entertainment", "Other"
)

@HiltViewModel
class ReceiptDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: Long = savedStateHandle["expenseId"] ?: 0L

    private val _uiState = MutableStateFlow(ReceiptDetailUiState())
    val uiState: StateFlow<ReceiptDetailUiState> = _uiState.asStateFlow()

    init {
        loadExpense()
    }

    private fun loadExpense() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val expense = expenseRepository.getById(expenseId)
            _uiState.update { it.copy(expense = expense, isLoading = false) }
        }
    }

    fun updateExpense(expense: Expense) {
        _uiState.update { it.copy(expense = expense) }
        viewModelScope.launch {
            expenseRepository.update(expense)
        }
    }

    fun updateMerchant(merchant: String) {
        val current = _uiState.value.expense ?: return
        updateExpense(current.copy(merchant = merchant))
    }

    fun updateAmount(amountStr: String) {
        val current = _uiState.value.expense ?: return
        val amount = amountStr.toDoubleOrNull() ?: return
        updateExpense(current.copy(amount = amount))
    }

    fun updateDate(dateMillis: Long) {
        val current = _uiState.value.expense ?: return
        updateExpense(current.copy(date = dateMillis))
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun updateCurrency(currency: String) {
        val current = _uiState.value.expense ?: return
        updateExpense(current.copy(currency = currency))
    }

    fun updateCategory(category: String) {
        val current = _uiState.value.expense ?: return
        updateExpense(current.copy(category = category))
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            expenseRepository.delete(expenseId)
            _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
        }
    }
}
