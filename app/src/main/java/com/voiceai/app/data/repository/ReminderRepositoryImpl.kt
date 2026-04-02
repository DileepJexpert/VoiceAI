package com.voiceai.app.data.repository

import com.voiceai.app.data.local.dao.ReminderDao
import com.voiceai.app.data.local.entity.ReminderEntity
import com.voiceai.app.domain.model.Reminder
import com.voiceai.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAll(): Flow<List<Reminder>> {
        return reminderDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Reminder? {
        return reminderDao.getById(id)?.toDomain()
    }

    override fun getPending(): Flow<List<Reminder>> {
        return reminderDao.getPending().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(reminder: Reminder): Long {
        return reminderDao.insert(reminder.toEntity())
    }

    override suspend fun update(reminder: Reminder) {
        reminderDao.update(reminder.toEntity())
    }

    override suspend fun markCompleted(id: Long) {
        reminderDao.markCompleted(id)
    }

    override suspend fun delete(id: Long) {
        reminderDao.deleteById(id)
    }

    private fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            id = id,
            title = title,
            description = description,
            sourceNoteId = sourceNoteId,
            sourceScanId = sourceScanId,
            reminderTime = reminderTime,
            isCompleted = isCompleted,
            calendarEventId = calendarEventId,
            createdAt = createdAt
        )
    }

    private fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            id = id,
            title = title,
            description = description,
            sourceNoteId = sourceNoteId,
            sourceScanId = sourceScanId,
            reminderTime = reminderTime,
            isCompleted = isCompleted,
            calendarEventId = calendarEventId,
            createdAt = createdAt
        )
    }
}
