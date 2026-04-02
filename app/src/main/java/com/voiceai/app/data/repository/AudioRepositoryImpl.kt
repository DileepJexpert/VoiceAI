package com.voiceai.app.data.repository

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import com.voiceai.app.domain.repository.AudioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRepository {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String = ""
    private val _isRecording = MutableStateFlow(false)
    private var isPaused = false

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override suspend fun startRecording(filePath: String) {
        currentFilePath = filePath
        mediaRecorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(filePath)
            prepare()
            start()
        }
        isPaused = false
        _isRecording.value = true
    }

    override suspend fun stopRecording(): String {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: RuntimeException) {
                // stop() can throw if called before any data was recorded
            }
            release()
        }
        mediaRecorder = null
        isPaused = false
        _isRecording.value = false
        return currentFilePath
    }

    override suspend fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            isPaused = true
        }
    }

    override suspend fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            isPaused = false
        }
    }

    override fun getAmplitude(): Flow<Int> = flow {
        while (true) {
            val amplitude = if (_isRecording.value && !isPaused) {
                try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (e: IllegalStateException) {
                    0
                }
            } else {
                0
            }
            emit(amplitude)
            delay(100L)
        }
    }

    override fun isRecording(): Flow<Boolean> = _isRecording.asStateFlow()
}
