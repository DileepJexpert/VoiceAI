package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.repository.ActionItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageActionItemsUseCase @Inject constructor(
    private val actionItemRepository: ActionItemRepository
) {
    fun getAll(): Flow<List<ActionItem>> = actionItemRepository.getAll()

    suspend fun getById(id: Long): ActionItem? = actionItemRepository.getById(id)

    fun getByStatus(status: String): Flow<List<ActionItem>> = actionItemRepository.getByStatus(status)

    fun getPending(): Flow<List<ActionItem>> = actionItemRepository.getPending()

    fun getOverdue(): Flow<List<ActionItem>> = actionItemRepository.getOverdue()

    suspend fun insert(item: ActionItem): Long = actionItemRepository.insert(item)

    suspend fun update(item: ActionItem) = actionItemRepository.update(item)

    suspend fun updateStatus(id: Long, status: String) = actionItemRepository.updateStatus(id, status)

    suspend fun delete(id: Long) = actionItemRepository.delete(id)
}
