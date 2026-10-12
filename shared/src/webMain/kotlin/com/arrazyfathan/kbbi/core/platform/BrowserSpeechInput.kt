@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.arrazyfathan.kbbi.core.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

internal class BrowserSpeechInput : SpeechInput {
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
    ): CapabilityResult<List<String>> {
        if (!speechRecognitionAvailable()) return CapabilityResult.Unavailable
        var session: JsAny? = null
        try {
            return withTimeoutOrNull(20_000L.milliseconds) {
                suspendCancellableCoroutine<CapabilityResult<List<String>>> { continuation ->
                    fun finish(result: CapabilityResult<List<String>>) {
                        if (continuation.isActive) continuation.resume(result)
                    }
                    session =
                        startSpeechRecognition(
                            onReady = { if (continuation.isActive) onReady() },
                            onPartial = { payload ->
                                if (continuation.isActive) onPartial(decodeCandidates(payload))
                            },
                            onResult = { payload ->
                                finish(
                                    CapabilityResult.Success(
                                        decodeCandidates(payload),
                                    ),
                                )
                            },
                            onError = { code ->
                                finish(
                                    when (code.toString()) {
                                        "not-allowed", "service-not-allowed" -> CapabilityResult.Denied
                                        "no-speech" -> CapabilityResult.Failed(FailureReason.SPEECH_EMPTY)
                                        "language-not-supported" -> CapabilityResult.Unavailable
                                        "InvalidStateError" -> CapabilityResult.Failed(FailureReason.SPEECH_BUSY)
                                        else -> CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
                                    },
                                )
                            },
                            onEnd = { finish(CapabilityResult.Success(emptyList())) },
                        )
                }
            } ?: CapabilityResult.Failed(FailureReason.SPEECH_EMPTY)
        } finally {
            session?.let(::abortSpeechRecognition)
        }
    }
}

private fun decodeCandidates(payload: JsString): List<String> =
    payload
        .toString()
        .takeIf { it.length <= 8192 }
        ?.let {
            runCatching { Json.decodeFromString<List<String>>(it) }.getOrNull()?.take(5)
        }.orEmpty()

private fun speechRecognitionAvailable(): Boolean =
    js("!!(globalThis.isSecureContext && (globalThis.SpeechRecognition || globalThis.webkitSpeechRecognition))")

private fun startSpeechRecognition(
    onReady: () -> Unit,
    onPartial: (JsString) -> Unit,
    onResult: (JsString) -> Unit,
    onError: (JsString) -> Unit,
    onEnd: () -> Unit,
): JsAny =
    js(
        """(() => {
    const Recognition = globalThis.SpeechRecognition || globalThis.webkitSpeechRecognition;
    const session = new Recognition();
    session.lang = 'id-ID';
    session.interimResults = true;
    session.continuous = false;
    session.maxAlternatives = 5;
    session.onstart = () => onReady();
    session.onresult = event => {
        const result = event.results[event.resultIndex];
        const candidates = JSON.stringify(Array.from(result).map(item => item.transcript));
        if (result.isFinal) onResult(candidates); else onPartial(candidates);
    };
    session.onerror = event => onError(event.error);
    session.onend = () => onEnd();
    try { session.start(); } catch (error) { onError(error.name || 'unknown'); }
    return session;
})()""",
    )

private fun abortSpeechRecognition(session: JsAny): Unit =
    js(
        "{ session.onstart = null; session.onresult = null; session.onerror = null; session.onend = null; try { session.abort(); } catch (_) {} }",
    )
