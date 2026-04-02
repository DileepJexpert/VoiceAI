package com.voiceai.app.domain.repository

import com.voiceai.app.domain.model.VoiceNote
import kotlinx.coroutines.flow.Flow

interface VoiceNoteRepository {
    fun getAllNotes(): Flow<List<VoiceNote>>
    suspend fun getNoteById(id: Long): VoiceNote?
    fun searchNotes(query: String): Flow<List<VoiceNote>>
    suspend fun insertNote(note: VoiceNote): Long
    suspend fun updateNote(note: VoiceNote)
    suspend fun deleteNote(id: Long)
    suspend fun toggleFavorite(id: Long)
}
