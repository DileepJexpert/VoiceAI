package com.voiceai.app.data.repository

import com.voiceai.app.data.local.dao.ActionItemDao
import com.voiceai.app.data.local.entity.ActionItemEntity
import com.voiceai.app.domain.model.ActionItem
import com.voiceai.app.domain.repository.ActionItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionItemRepositoryImpl @Inject constructor(
    private val actionItemDao: ActionItemDao
) : ActionItemRepository {

    override fun getAll(): Flow<List<ActionItem>> {
        return actionItemDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): ActionItem? {
        return actionItemDao.getById(id)?.toDomain()
    }

    override fun getByStatus(status: String): Flow<List<ActionItem>> {
        return actionItemDao.getByStatus(status).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPending(): Flow<List<ActionItem>> {
        return actionItemDao.getPending().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOverdue(): Flow<List<ActionItem>> {
        return actionItemDao.getOverdue().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(item: ActionItem): Long {
        return actionItemDao.insert(item.toEntity())
    }

    override suspend fun update(item: ActionItem) {
        actionItemDao.update(item.toEntity())
    }

    override suspend fun updateStatus(id: Long, status: String) {
        actionItemDao.updateStatus(id, status)
    }

    override suspend fun delete(id: Long) {
        actionItemDao.deleteById(id)
    }

    private fun ActionItemEntity.toDomain(): ActionItem {
        return ActionItem(
            id = id,
            title = title,
            sourceNoteId = sourceNoteId,
            sourceScanId = sourceScanId,
            status = status,
            priority = priority,
            dueDate = dueDate,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun ActionItem.toEntity(): ActionItemEntity {
        return ActionItemEntity(
            id = id,
            title = title,
            sourceNoteId = sourceNoteId,
            sourceScanId = sourceScanId,
            status = status,
            priority = priority,
            dueDate = dueDate,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }
}
