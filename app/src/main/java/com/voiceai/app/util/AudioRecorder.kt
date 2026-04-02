package com.voiceai.app.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor(
    private val context: Context
) {
    private var recorder: MediaRecorder? = null
    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    fun startRecording(filePath: String) {
        val file = File(filePath)
        file.parentFile?.mkdirs()

        recorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(Constants.AUDIO_SAMPLE_RATE)
            setAudioEncodingBitRate(128000)
            setOutputFile(filePath)
            prepare()
            start()
        }
        _isRecording.value = true
    }

    fun stopRecording(): String {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        _isRecording.value = false
        return ""
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
        }
    }

    fun getMaxAmplitude(): Int {
        return recorder?.maxAmplitude ?: 0
    }

    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }
}
