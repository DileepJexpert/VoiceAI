package com.voiceai.app.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.CategoryTotal
import com.voiceai.app.domain.model.Expense
import com.voiceai.app.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ExpenseTrackerUiState(
    val expenses: List<Expense> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ExpenseTrackerViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseTrackerUiState())
    val uiState: StateFlow<ExpenseTrackerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val state = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.selectedYear)
            set(Calendar.MONTH, state.selectedMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = calendar.timeInMillis

        viewModelScope.launch {
            expenseRepository.getByDateRange(startOfMonth, endOfMonth).collect { expenses ->
                val filtered = if (state.selectedCategory != null) {
                    expenses.filter { it.category == state.selectedCategory }
                } else {
                    expenses
                }
                _uiState.update { it.copy(expenses = filtered, isLoading = false) }
            }
        }

        viewModelScope.launch {
            expenseRepository.getCategoryTotals().collect { totals ->
                _uiState.update { it.copy(categoryTotals = totals) }
            }
        }

        viewModelScope.launch {
            expenseRepository.getMonthlyTotal(state.selectedYear, state.selectedMonth).collect { total ->
                _uiState.update { it.copy(monthlyTotal = total) }
            }
        }
    }

    fun setMonth(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month, isLoading = true) }
        loadData()
    }

    fun setCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadData()
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            expenseRepository.delete(id)
        }
    }

    fun previousMonth() {
        val state = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.selectedYear)
            set(Calendar.MONTH, state.selectedMonth - 1)
            add(Calendar.MONTH, -1)
        }
        setMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }

    fun nextMonth() {
        val state = _uiState.value
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, state.selectedYear)
            set(Calendar.MONTH, state.selectedMonth - 1)
            add(Calendar.MONTH, 1)
        }
        setMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1)
    }
}
