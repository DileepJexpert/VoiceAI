package com.voiceai.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.voiceai.app.data.local.entity.NoteTagCrossRef
import com.voiceai.app.data.local.entity.ScanTagCrossRef
import com.voiceai.app.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Transaction
    @Query("SELECT t.* FROM tags t INNER JOIN note_tag_cross_ref ntc ON t.id = ntc.tag_id WHERE ntc.note_id = :noteId")
    fun getTagsForNote(noteId: Long): Flow<List<TagEntity>>

    @Transaction
    @Query("SELECT t.* FROM tags t INNER JOIN scan_tag_cross_ref stc ON t.id = stc.tag_id WHERE stc.scan_id = :scanId")
    fun getTagsForScan(scanId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteTag(crossRef: NoteTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanTag(crossRef: ScanTagCrossRef)

    @Delete
    suspend fun deleteNoteTag(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteScanTag(crossRef: ScanTagCrossRef)
}
