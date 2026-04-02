package com.voiceai.app.data.repository

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import com.voiceai.app.domain.repository.TtsRepository
import com.voiceai.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TtsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TtsRepository {

    private var tts: TextToSpeech? = null
    private val _isPlaying = MutableStateFlow(false)
    private var isInitialized = false
    private var pendingText: String? = null
    private var speechRate: Float = Constants.TTS_DEFAULT_SPEED

    private suspend fun ensureInitialized() {
        if (isInitialized && tts != null) return

        suspendCancellableCoroutine { continuation ->
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    tts?.setSpeechRate(speechRate)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isPlaying.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isPlaying.value = false
                        }

                        @Deprecated("Deprecated in API")
                        override fun onError(utteranceId: String?) {
                            _isPlaying.value = false
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isPlaying.value = false
                        }
                    })
                    isInitialized = true
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            RuntimeException("TextToSpeech initialization failed with status: $status")
                        )
                    }
                }
            }

            continuation.invokeOnCancellation {
                tts?.shutdown()
                tts = null
                isInitialized = false
            }
        }
    }

    override suspend fun speak(text: String) {
        ensureInitialized()
        pendingText = text
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override suspend fun pause() {
        // Android TTS does not natively support pause, so we stop and store text
        tts?.stop()
        _isPlaying.value = false
    }

    override suspend fun resume() {
        // Re-speak the pending text as a workaround for missing native pause
        pendingText?.let { speak(it) }
    }

    override suspend fun stop() {
        tts?.stop()
        pendingText = null
        _isPlaying.value = false
    }

    override suspend fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(Constants.TTS_MIN_SPEED, Constants.TTS_MAX_SPEED)
        tts?.setSpeechRate(speechRate)
    }

    override fun isPlaying(): Flow<Boolean> = _isPlaying.asStateFlow()
}
