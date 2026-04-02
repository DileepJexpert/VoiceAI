package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.CategoryTotal
import com.voiceai.app.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getAll(): Flow<List<Expense>>
    suspend fun getById(id: Long): Expense?
    fun getByCategory(category: String): Flow<List<Expense>>
    fun getByDateRange(start: Long, end: Long): Flow<List<Expense>>
    fun getCategoryTotals(): Flow<List<CategoryTotal>>
    fun getMonthlyTotal(year: Int, month: Int): Flow<Double>
    suspend fun insert(expense: Expense): Long
    suspend fun update(expense: Expense)
    suspend fun delete(id: Long)
}
