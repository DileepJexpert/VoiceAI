package com.voiceai.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.voiceai.app.data.local.dao.ExpenseDao
import com.voiceai.app.data.local.entity.ExpenseEntity
import com.voiceai.app.domain.model.CategoryTotal
import com.voiceai.app.domain.model.Expense
import com.voiceai.app.domain.model.ExpenseLineItem
import com.voiceai.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    private val gson = Gson()

    override fun getAll(): Flow<List<Expense>> {
        return expenseDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Expense? {
        return expenseDao.getById(id)?.toDomain()
    }

    override fun getByCategory(category: String): Flow<List<Expense>> {
        return expenseDao.getByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getByDateRange(start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategoryTotals(): Flow<List<CategoryTotal>> {
        return expenseDao.getTotalByCategory().map { daoTotals ->
            daoTotals.map { CategoryTotal(category = it.category, total = it.total) }
        }
    }

    override fun getMonthlyTotal(year: Int, month: Int): Flow<Double> {
        val yearStr = year.toString()
        val monthStr = month.toString().padStart(2, '0')
        return expenseDao.getMonthlyTotal(yearStr, monthStr)
    }

    override suspend fun insert(expense: Expense): Long {
        return expenseDao.insert(expense.toEntity())
    }

    override suspend fun update(expense: Expense) {
        expenseDao.update(expense.toEntity())
    }

    override suspend fun delete(id: Long) {
        expenseDao.deleteById(id)
    }

    private fun ExpenseEntity.toDomain(): Expense {
        val lineItems: List<ExpenseLineItem> = if (!items.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<ExpenseLineItem>>() {}.type
                gson.fromJson(items, type)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return Expense(
            id = id,
            merchant = merchant,
            amount = amount,
            currency = currency,
            date = date,
            category = category,
            items = lineItems,
            scanId = scanId,
            imagePath = imagePath,
            createdAt = createdAt
        )
    }

    private fun Expense.toEntity(): ExpenseEntity {
        val itemsJson = if (items.isNotEmpty()) {
            gson.toJson(items)
        } else {
            null
        }

        return ExpenseEntity(
            id = id,
            merchant = merchant,
            amount = amount,
            currency = currency,
            date = date,
            category = category,
            items = itemsJson,
            scanId = scanId,
            imagePath = imagePath,
            createdAt = createdAt
        )
    }
}
