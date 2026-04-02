package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.AIChatRepository
import com.voiceai.app.domain.repository.ChatMessage
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AIChatUseCase @Inject constructor(
    private val aiChatRepository: AIChatRepository,
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository
) {
    suspend fun sendMessage(message: String): Flow<String> {
        val notes = voiceNoteRepository.getAllNotes().first()
        val documents = scannedDocumentRepository.getAllDocuments().first()

        val context = mutableListOf<String>()
        notes.forEach { note ->
            note.transcript?.let { context.add(it) }
            note.summary?.let { context.add(it) }
        }
        documents.forEach { doc ->
            doc.extractedText?.let { context.add(it) }
            doc.summary?.let { context.add(it) }
        }

        return aiChatRepository.sendMessage(message, context)
    }

    suspend fun getConversationHistory(): List<ChatMessage> {
        return aiChatRepository.getConversationHistory()
    }
}
