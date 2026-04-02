package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.model.Reminder
import com.voiceai.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    fun getAll(): Flow<List<Reminder>> = reminderRepository.getAll()

    suspend fun getById(id: Long): Reminder? = reminderRepository.getById(id)

    fun getPending(): Flow<List<Reminder>> = reminderRepository.getPending()

    suspend fun insert(reminder: Reminder): Long = reminderRepository.insert(reminder)

    suspend fun update(reminder: Reminder) = reminderRepository.update(reminder)

    suspend fun markCompleted(id: Long) = reminderRepository.markCompleted(id)

    suspend fun delete(id: Long) = reminderRepository.delete(id)
}
