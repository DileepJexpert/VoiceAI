package com.voiceai.app.di

import android.content.Context
import androidx.room.Room
import com.voiceai.app.data.local.AppDatabase
import com.voiceai.app.data.local.dao.FolderDao
import com.voiceai.app.data.local.dao.ScannedDocumentDao
import com.voiceai.app.data.local.dao.ScannedPageDao
import com.voiceai.app.data.local.dao.TagDao
import com.voiceai.app.data.local.dao.VoiceNoteDao
import com.voiceai.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).build()

    @Provides
    fun provideVoiceNoteDao(db: AppDatabase): VoiceNoteDao = db.voiceNoteDao()

    @Provides
    fun provideScannedDocumentDao(db: AppDatabase): ScannedDocumentDao = db.scannedDocumentDao()

    @Provides
    fun provideScannedPageDao(db: AppDatabase): ScannedPageDao = db.scannedPageDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()
}
