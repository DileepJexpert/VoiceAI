package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAll(): Flow<List<Reminder>>
    suspend fun getById(id: Long): Reminder?
    fun getPending(): Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder): Long
    suspend fun update(reminder: Reminder)
    suspend fun markCompleted(id: Long)
    suspend fun delete(id: Long)
}
