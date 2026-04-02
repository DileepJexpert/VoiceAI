package com.voiceai.app.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechManager @Inject constructor(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private var currentTextLength = 0

    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    _progress.value = 0f
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _progress.value = 1f
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _progress.value = 0f
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    _progress.value = 0f
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    if (currentTextLength > 0) {
                        _progress.value = end.toFloat() / currentTextLength
                    }
                }
            })
            isInitialized = true

            // Speak any text that was queued before initialization completed
            pendingText?.let { text ->
                pendingText = null
                speak(text)
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            pendingText = text
            initialize()
            return
        }

        currentTextLength = text.length
        _progress.value = 0f

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "voiceai_tts_${System.currentTimeMillis()}")
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }

    fun pause() {
        // Android TTS does not natively support pause; stop is used as a workaround
        tts?.stop()
        _isSpeaking.value = false
    }

    fun resume() {
        // Android TTS does not support resume natively.
        // Callers should re-invoke speak() with the remaining text if needed.
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _progress.value = 0f
        currentTextLength = 0
    }

    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(Constants.TTS_MIN_SPEED, Constants.TTS_MAX_SPEED)
        tts?.setSpeechRate(clampedRate)
    }

    fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(clampedPitch)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        _isSpeaking.value = false
        _progress.value = 0f
    }
}
