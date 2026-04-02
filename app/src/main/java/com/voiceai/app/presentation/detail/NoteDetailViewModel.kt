package com.voiceai.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.VoiceNote
import com.voiceai.app.domain.repository.VoiceNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteDetailUiState(
    val note: VoiceNote? = null,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val audioDuration: Long = 0L,
    val playbackSpeed: Float = 1f,
    val selectedTab: Int = 0,
    val isTtsSpeaking: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle["noteId"] ?: 0L

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val note = voiceNoteRepository.getNoteById(noteId)
            _uiState.update {
                it.copy(
                    note = note,
                    isLoading = false,
                    audioDuration = note?.duration ?: 0L
                )
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun playAudio() {
        _uiState.update { it.copy(isPlaying = true) }
        // Actual MediaPlayer integration would be wired here
    }

    fun pauseAudio() {
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) pauseAudio() else playAudio()
    }

    fun seekTo(position: Long) {
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            voiceNoteRepository.toggleFavorite(noteId)
            // Reload note to reflect the updated favorite state
            val updatedNote = voiceNoteRepository.getNoteById(noteId)
            _uiState.update { it.copy(note = updatedNote) }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteNote() {
        viewModelScope.launch {
            voiceNoteRepository.deleteNote(noteId)
            _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
        }
    }

    fun updateTitle(title: String) {
        val currentNote = _uiState.value.note ?: return
        val updatedNote = currentNote.copy(title = title)
        _uiState.update { it.copy(note = updatedNote) }
        viewModelScope.launch {
            voiceNoteRepository.updateNote(updatedNote)
        }
    }

    fun startTts() {
        _uiState.update { it.copy(isTtsSpeaking = true) }
        // Actual TTS engine integration would be wired here
    }

    fun stopTts() {
        _uiState.update { it.copy(isTtsSpeaking = false) }
    }

    fun toggleTts() {
        if (_uiState.value.isTtsSpeaking) stopTts() else startTts()
    }
}
