package com.voiceai.app.domain.usecase

import com.voiceai.app.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecordAudioUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend fun startRecording(filePath: String) {
        audioRepository.startRecording(filePath)
    }

    suspend fun stopRecording(): String {
        return audioRepository.stopRecording()
    }

    suspend fun pauseRecording() {
        audioRepository.pauseRecording()
    }

    suspend fun resumeRecording() {
        audioRepository.resumeRecording()
    }

    fun getAmplitude(): Flow<Int> {
        return audioRepository.getAmplitude()
    }
}
