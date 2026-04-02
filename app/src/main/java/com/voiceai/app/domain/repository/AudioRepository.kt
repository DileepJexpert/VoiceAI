package com.voiceai.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun startRecording(filePath: String)
    suspend fun stopRecording(): String
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getAmplitude(): Flow<Int>
    fun isRecording(): Flow<Boolean>
}
