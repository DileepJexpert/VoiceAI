package com.voiceai.app.domain.model

data class Expense(
    val id: Long,
    val merchant: String,
    val amount: Double,
    val currency: String,
    val date: Long,
    val category: String?,
    val items: List<ExpenseLineItem> = emptyList(),
    val scanId: Long?,
    val imagePath: String?,
    val createdAt: Long
)

data class ExpenseLineItem(
    val name: String,
    val price: Double
)
