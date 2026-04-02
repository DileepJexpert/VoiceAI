package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.voiceai.app.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voiceNote: VoiceNoteEntity): Long

    @Update
    suspend fun update(voiceNote: VoiceNoteEntity)

    @Delete
    suspend fun delete(voiceNote: VoiceNoteEntity)

    @Query("SELECT * FROM voice_notes ORDER BY created_at DESC")
    fun getAll(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id")
    suspend fun getById(id: Long): VoiceNoteEntity?

    @Query("SELECT * FROM voice_notes WHERE title LIKE '%' || :query || '%' OR transcript LIKE '%' || :query || '%' ORDER BY created_at DESC")
    fun search(query: String): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE folder_id = :folderId ORDER BY created_at DESC")
    fun getByFolder(folderId: Long): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE is_favorite = 1 ORDER BY created_at DESC")
    fun getFavorites(): Flow<List<VoiceNoteEntity>>

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
