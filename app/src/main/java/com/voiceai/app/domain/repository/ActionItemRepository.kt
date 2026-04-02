package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.ActionItem
import kotlinx.coroutines.flow.Flow

interface ActionItemRepository {
    fun getAll(): Flow<List<ActionItem>>
    suspend fun getById(id: Long): ActionItem?
    fun getByStatus(status: String): Flow<List<ActionItem>>
    fun getPending(): Flow<List<ActionItem>>
    fun getOverdue(): Flow<List<ActionItem>>
    suspend fun insert(item: ActionItem): Long
    suspend fun update(item: ActionItem)
    suspend fun updateStatus(id: Long, status: String)
    suspend fun delete(id: Long)
}
