package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.model.ScannedDocument
import com.voiceai.app.domain.model.VoiceNote
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SearchResult(
    val voiceNotes: List<VoiceNote>,
    val scannedDocuments: List<ScannedDocument>
)

class SearchNotesUseCase @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository
) {
    operator fun invoke(query: String): Flow<SearchResult> {
        return voiceNoteRepository.searchNotes(query)
            .combine(scannedDocumentRepository.searchDocuments(query)) { notes, documents ->
                SearchResult(
                    voiceNotes = notes,
                    scannedDocuments = documents
                )
            }
    }
}
