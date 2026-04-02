package com.voiceai.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voiceai.app.domain.repository.ChatMessage
import com.voiceai.app.domain.usecase.AIChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val currentInput: String = ""
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiChatUseCase: AIChatUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = aiChatUseCase.getConversationHistory()
            _uiState.update { it.copy(messages = history) }
        }
    }

    fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input) }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            role = "user",
            content = text.trim(),
            timestamp = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                currentInput = ""
            )
        }

        viewModelScope.launch {
            val responseBuilder = StringBuilder()
            aiChatUseCase.sendMessage(text.trim())
                .catch { e ->
                    val errorMessage = ChatMessage(
                        role = "assistant",
                        content = "Sorry, I encountered an error: ${e.localizedMessage}",
                        timestamp = System.currentTimeMillis()
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMessage,
                            isLoading = false
                        )
                    }
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)
                }

            if (responseBuilder.isNotEmpty()) {
                val assistantMessage = ChatMessage(
                    role = "assistant",
                    content = responseBuilder.toString(),
                    timestamp = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + assistantMessage,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearChat() {
        _uiState.update { AIChatUiState() }
    }
}
