package com.arrazyfathan.kbbi.core.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

internal class AndroidSpeechInput(
    private val context: Context,
) : SpeechInput {
    private var busy = false

    override suspend fun recognize(): CapabilityResult<String> =
        when (val result = recognizeCandidates {}) {
            is CapabilityResult.Success -> {
                CapabilityResult.Success(
                    result.value.firstOrNull().orEmpty(),
                )
            }

            is CapabilityResult.Failed -> {
                result
            }

            CapabilityResult.Denied -> {
                CapabilityResult.Denied
            }

            CapabilityResult.Unavailable -> {
                CapabilityResult.Unavailable
            }
        }

    override suspend fun recognizeCandidates(
        onReady: () -> Unit,
        onPartial: (List<String>) -> Unit,
    ): CapabilityResult<List<String>> =
        withContext(Dispatchers.Main.immediate) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) return@withContext CapabilityResult.Unavailable
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return@withContext CapabilityResult.Denied
            }
            if (busy) return@withContext CapabilityResult.Failed(FailureReason.SPEECH_BUSY)
            busy = true
            var recognizer: SpeechRecognizer? = null
            try {
                withTimeoutOrNull(20_000L.milliseconds) {
                    suspendCancellableCoroutine<CapabilityResult<List<String>>> { continuation ->
                        val speech = SpeechRecognizer.createSpeechRecognizer(context)
                        recognizer = speech

                        fun finish(result: CapabilityResult<List<String>>) {
                            if (continuation.isActive) continuation.resume(result)
                        }
                        speech.setRecognitionListener(
                            object : RecognitionListener {
                                override fun onReadyForSpeech(params: Bundle?) {}

                                override fun onBeginningOfSpeech() {}

                                override fun onRmsChanged(rmsdB: Float) {}

                                override fun onBufferReceived(buffer: ByteArray?) {}

                                override fun onEndOfSpeech() {}

                                override fun onEvent(
                                    eventType: Int,
                                    params: Bundle?,
                                ) {}

                                override fun onPartialResults(partialResults: Bundle?) {
                                    if (continuation.isActive) onPartial(partialResults.candidates())
                                }

                                override fun onResults(results: Bundle?) {
                                    finish(CapabilityResult.Success(results.candidates()))
                                }

                                override fun onError(error: Int) {
                                    finish(
                                        when (error) {
                                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                                                CapabilityResult.Denied
                                            }

                                            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                                CapabilityResult.Failed(
                                                    FailureReason.SPEECH_EMPTY,
                                                )
                                            }

                                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                                                CapabilityResult.Failed(
                                                    FailureReason.SPEECH_BUSY,
                                                )
                                            }

                                            else -> {
                                                CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
                                            }
                                        },
                                    )
                                }
                            },
                        )
                        onReady()
                        speech.startListening(
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
                                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                                putExtra(
                                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                                    3000L,
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                                    2500L,
                                )
                                putExtra(
                                    RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                                    2000L,
                                )
                            },
                        )
                    }
                } ?: CapabilityResult.Failed(FailureReason.SPEECH_EMPTY)
            } finally {
                recognizer?.cancel()
                recognizer?.destroy()
                busy = false
            }
        }
}

private fun Bundle?.candidates(): List<String> =
    this
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.filter { it.isNotBlank() }
        .orEmpty()
