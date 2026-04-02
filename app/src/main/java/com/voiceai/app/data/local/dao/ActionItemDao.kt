package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.ActionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(actionItem: ActionItemEntity): Long

    @Update
    suspend fun update(actionItem: ActionItemEntity)

    @Delete
    suspend fun delete(actionItem: ActionItemEntity)

    @Query("SELECT * FROM action_items ORDER BY created_at DESC")
    fun getAll(): Flow<List<ActionItemEntity>>

    @Query("SELECT * FROM action_items WHERE id = :id")
    suspend fun getById(id: Long): ActionItemEntity?

    @Query("SELECT * FROM action_items WHERE status = :status")
    fun getByStatus(status: String): Flow<List<ActionItemEntity>>

    @Query("SELECT * FROM action_items WHERE source_note_id = :noteId")
    fun getBySourceNote(noteId: Long): Flow<List<ActionItemEntity>>

    @Query("SELECT * FROM action_items WHERE source_scan_id = :scanId")
    fun getBySourceScan(scanId: Long): Flow<List<ActionItemEntity>>

    @Query("SELECT * FROM action_items WHERE status != 'done'")
    fun getPending(): Flow<List<ActionItemEntity>>

    @Query("SELECT * FROM action_items WHERE due_date < :now AND status != 'done'")
    fun getOverdue(now: Long = System.currentTimeMillis()): Flow<List<ActionItemEntity>>

    @Query("UPDATE action_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM action_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
