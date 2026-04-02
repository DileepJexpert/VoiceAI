package com.voiceai.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voiceai.app.data.local.dao.FolderDao
import com.voiceai.app.data.local.dao.ScannedDocumentDao
import com.voiceai.app.data.local.dao.ScannedPageDao
import com.voiceai.app.data.local.dao.TagDao
import com.voiceai.app.data.local.dao.VoiceNoteDao
import com.voiceai.app.data.local.entity.FolderEntity
import com.voiceai.app.data.local.entity.NoteTagCrossRef
import com.voiceai.app.data.local.entity.ScanTagCrossRef
import com.voiceai.app.data.local.entity.ScannedDocumentEntity
import com.voiceai.app.data.local.entity.ScannedPageEntity
import com.voiceai.app.data.local.entity.TagEntity
import com.voiceai.app.data.local.entity.VoiceNoteEntity

@Database(
    entities = [
        VoiceNoteEntity::class,
        ScannedDocumentEntity::class,
        ScannedPageEntity::class,
        TagEntity::class,
        FolderEntity::class,
        NoteTagCrossRef::class,
        ScanTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun scannedDocumentDao(): ScannedDocumentDao
    abstract fun scannedPageDao(): ScannedPageDao
    abstract fun tagDao(): TagDao
    abstract fun folderDao(): FolderDao
}
