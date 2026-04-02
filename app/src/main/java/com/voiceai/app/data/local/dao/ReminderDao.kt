package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY reminder_time ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE is_completed = 0 ORDER BY reminder_time ASC")
    fun getPending(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE source_note_id = :noteId")
    fun getBySourceNote(noteId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE source_scan_id = :scanId")
    fun getBySourceScan(scanId: Long): Flow<List<ReminderEntity>>

    @Query("UPDATE reminders SET is_completed = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
