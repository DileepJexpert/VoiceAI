package com.voiceai.app.presentation.record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.model.VoiceNote
import com.voiceai.app.domain.repository.VoiceNoteRepository
import com.voiceai.app.domain.usecase.RecordAudioUseCase
import com.voiceai.app.domain.usecase.SummarizeNoteUseCase
import com.voiceai.app.domain.usecase.TranscribeAudioUseCase
import com.voiceai.app.util.Constants
import com.voiceai.app.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val duration: Long = 0L,
    val amplitudes: List<Float> = emptyList(),
    val isSaving: Boolean = false,
    val isTranscribing: Boolean = false,
    val savedNoteId: Long? = null,
    val templateType: String = "general",
    val isLiveTranscribing: Boolean = false,
    val liveTranscript: String = "",
    val bookmarks: List<Long> = emptyList()
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordAudioUseCase: RecordAudioUseCase,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val voiceNoteRepository: VoiceNoteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var amplitudeJob: Job? = null
    private var currentFilePath: String? = null

    fun startRecording() {
        val fileName = FileUtils.generateFileName("recording", Constants.AUDIO_FORMAT)
        val filePath = FileUtils.getAudioFilePath(context, fileName)
        currentFilePath = filePath

        viewModelScope.launch {
            recordAudioUseCase.startRecording(filePath)
            _uiState.update {
                it.copy(
                    isRecording = true,
                    isPaused = false,
                    duration = 0L,
                    amplitudes = emptyList()
                )
            }
            startTimer()
            collectAmplitudes()
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            recordAudioUseCase.pauseRecording()
            timerJob?.cancel()
            amplitudeJob?.cancel()
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            recordAudioUseCase.resumeRecording()
            _uiState.update { it.copy(isPaused = false) }
            startTimer()
            collectAmplitudes()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val filePath = recordAudioUseCase.stopRecording()
            currentFilePath = filePath
            timerJob?.cancel()
            amplitudeJob?.cancel()
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isPaused = false
                )
            }
        }
    }

    fun saveNote(title: String) {
        val filePath = currentFilePath ?: return
        val state = _uiState.value
        val duration = state.duration

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val bookmarksJson = JSONArray(state.bookmarks).toString()
            val note = VoiceNote(
                id = 0L,
                title = title,
                audioFilePath = filePath,
                transcript = null,
                summary = null,
                duration = duration,
                createdAt = now,
                updatedAt = now,
                language = Constants.DEFAULT_LANGUAGE,
                isFavorite = false,
                folderId = null,
                tags = emptyList(),
                templateType = state.templateType,
                bookmarks = state.bookmarks
            )

            val noteId = voiceNoteRepository.insertNote(note)
            _uiState.update { it.copy(isSaving = false, savedNoteId = noteId) }

            // Transcribe and summarize in the background
            launch {
                try {
                    _uiState.update { it.copy(isTranscribing = true) }
                    val transcript = transcribeAudioUseCase(filePath, Constants.DEFAULT_LANGUAGE)
                    val updatedNote = note.copy(
                        id = noteId,
                        transcript = transcript,
                        updatedAt = System.currentTimeMillis()
                    )
                    voiceNoteRepository.updateNote(updatedNote)

                    val summary = summarizeNoteUseCase(transcript)
                    val summarizedNote = updatedNote.copy(
                        summary = summary,
                        updatedAt = System.currentTimeMillis()
                    )
                    voiceNoteRepository.updateNote(summarizedNote)
                } catch (_: Exception) {
                    // Background transcription/summarization failure is non-fatal
                } finally {
                    _uiState.update { it.copy(isTranscribing = false) }
                }
            }
        }
    }

    fun setTemplate(type: String) {
        _uiState.update { it.copy(templateType = type) }
    }

    fun addBookmark() {
        _uiState.update { state ->
            state.copy(bookmarks = state.bookmarks + state.duration)
        }
    }

    fun toggleLiveTranscription() {
        _uiState.update { it.copy(isLiveTranscribing = !it.isLiveTranscribing) }
    }

    fun discardRecording() {
        timerJob?.cancel()
        amplitudeJob?.cancel()
        currentFilePath?.let { FileUtils.deleteFile(it) }
        currentFilePath = null
        _uiState.update { RecordUiState() }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(100L)
                _uiState.update { it.copy(duration = it.duration + 100L) }
            }
        }
    }

    private fun collectAmplitudes() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch {
            recordAudioUseCase.getAmplitude().collect { rawAmplitude ->
                val normalized = (rawAmplitude / 32768f).coerceIn(0f, 1f)
                _uiState.update { state ->
                    val updated = state.amplitudes.toMutableList().apply { add(normalized) }
                    // Keep only the most recent amplitudes for display
                    val trimmed = if (updated.size > Constants.WAVEFORM_SPIKE_COUNT) {
                        updated.takeLast(Constants.WAVEFORM_SPIKE_COUNT)
                    } else {
                        updated
                    }
                    state.copy(amplitudes = trimmed)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        amplitudeJob?.cancel()
    }
}
