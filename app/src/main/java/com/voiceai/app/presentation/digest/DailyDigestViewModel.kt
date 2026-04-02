package com.voiceai.app.presentation.digest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.repository.ActionItemRepository
import com.voiceai.app.domain.repository.ScannedDocumentRepository
import com.voiceai.app.domain.repository.VoiceNoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class RecentActivity(
    val id: Long,
    val title: String,
    val type: String, // "voice_note", "scan", "action_item"
    val timestamp: Long
)

data class DailyDigestUiState(
    val yesterdayRecordings: Int = 0,
    val pendingActions: Int = 0,
    val upcomingReminders: Int = 0,
    val weeklyNoteCount: Int = 0,
    val totalRecordingTime: String = "0m",
    val recentActivity: List<RecentActivity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DailyDigestViewModel @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val scannedDocumentRepository: ScannedDocumentRepository,
    private val actionItemRepository: ActionItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyDigestUiState())
    val uiState: StateFlow<DailyDigestUiState> = _uiState.asStateFlow()

    init {
        loadDigest()
    }

    private fun loadDigest() {
        viewModelScope.launch {
            val allNotes = voiceNoteRepository.getAllNotes().first()
            val allDocs = scannedDocumentRepository.getAllDocuments().first()
            val pendingItems = actionItemRepository.getPending().first()

            val now = System.currentTimeMillis()

            // Yesterday's recordings
            val yesterdayStart = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val yesterdayRecordings = allNotes.count { it.createdAt in yesterdayStart until todayStart }

            // This week's notes
            val weekStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val weeklyNoteCount = allNotes.count { it.createdAt >= weekStart }

            // Total recording time
            val totalDurationMs = allNotes.sumOf { it.duration }
            val totalMinutes = totalDurationMs / 60000
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            val totalRecordingTime = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

            // Pending actions
            val pendingCount = pendingItems.size

            // Recent activity (last 5 items from notes and docs combined)
            val recentNotes = allNotes.take(5).map {
                RecentActivity(
                    id = it.id,
                    title = it.title,
                    type = "voice_note",
                    timestamp = it.createdAt
                )
            }
            val recentDocs = allDocs.take(5).map {
                RecentActivity(
                    id = it.id,
                    title = it.title,
                    type = "scan",
                    timestamp = it.createdAt
                )
            }
            val recentActivity = (recentNotes + recentDocs)
                .sortedByDescending { it.timestamp }
                .take(5)

            _uiState.update {
                it.copy(
                    yesterdayRecordings = yesterdayRecordings,
                    pendingActions = pendingCount,
                    upcomingReminders = 0, // Reminders loaded separately if needed
                    weeklyNoteCount = weeklyNoteCount,
                    totalRecordingTime = totalRecordingTime,
                    recentActivity = recentActivity,
                    isLoading = false
                )
            }
        }
    }
}
