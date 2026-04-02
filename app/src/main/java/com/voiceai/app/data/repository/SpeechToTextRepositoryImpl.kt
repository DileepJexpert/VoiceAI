package com.voiceai.app.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import com.voiceai.app.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class SpeechToTextRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechToTextRepository {

    override suspend fun transcribe(audioFilePath: String, language: String): String {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            throw UnsupportedOperationException("Speech recognition is not available on this device")
        }

        return suspendCancellableCoroutine { continuation ->
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    recognizer.destroy()
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            RuntimeException("Speech recognition error: $error")
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    recognizer.destroy()
                    val matches =
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (continuation.isActive) {
                        if (!matches.isNullOrEmpty()) {
                            continuation.resume(matches.first())
                        } else {
                            continuation.resumeWithException(
                                RuntimeException("No speech recognition results")
                            )
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            continuation.invokeOnCancellation {
                recognizer.destroy()
            }

            recognizer.startListening(intent)
        }
    }
}
