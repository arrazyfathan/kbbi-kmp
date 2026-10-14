@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
)

package com.arrazyfathan.kbbi.core.platform

import com.arrazyfathan.kbbi.core.logging.AppLogger
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionMixWithOthers
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.inputAvailable
import platform.AVFAudio.setActive
import platform.Foundation.NSError
import platform.Foundation.NSLocale
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUnderlyingErrorKey
import platform.Foundation.localeIdentifier
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import platform.UIKit.UIDevice
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

internal class IosSpeechInput : SpeechInput {
    private var busy = false

    // AVAudioSession activation and engine I/O can block; keep them off UIKit's thread.
    // All capture mutations are synchronous on this serial dispatcher.
    private val audioDispatcher = Dispatchers.Default.limitedParallelism(1)

    private suspend fun <T> audioOperation(block: () -> T): T = withContext(NonCancellable + audioDispatcher) { block() }

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
            if (busy) return@withContext CapabilityResult.Failed(FailureReason.SPEECH_BUSY)
            busy = true
            try {
                AppLogger.debug(
                    "KBBI-Speech",
                    "stage=start ios=${UIDevice.currentDevice.systemVersion} simulator=${NSProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] != null}",
                )
                val supportedLocales =
                    SFSpeechRecognizer.supportedLocales().mapNotNull { it as? NSLocale }
                val locale =
                    supportedLocales.firstOrNull {
                        it.localeIdentifier.replace('_', '-').lowercase() == "id-id"
                    } ?: supportedLocales.firstOrNull {
                        it.localeIdentifier
                            .replace('_', '-')
                            .lowercase()
                            .substringBefore('-') == "id"
                    }
                if (locale == null) {
                    AppLogger.warning("KBBI-Speech", "stage=locale Indonesian recognition unavailable")
                    return@withContext CapabilityResult.Unavailable
                }
                val authorized =
                    suspendCancellableCoroutine { continuation ->
                        SFSpeechRecognizer.requestAuthorization { status ->
                            if (continuation.isActive) {
                                continuation.resume(
                                    status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized,
                                )
                            }
                        }
                    }
                if (!authorized) return@withContext CapabilityResult.Denied
                val session = AVAudioSession.sharedInstance()
                val microphoneGranted =
                    suspendCancellableCoroutine { continuation ->
                        session.requestRecordPermission { granted ->
                            if (continuation.isActive) continuation.resume(granted)
                        }
                    }
                if (!microphoneGranted) return@withContext CapabilityResult.Denied
                val recognizer = SFSpeechRecognizer(locale = locale)
                recognizer.queue = NSOperationQueue.mainQueue
                if (!recognizer.available) {
                    AppLogger.warning("KBBI-Speech", "stage=recognizer service unavailable")
                    return@withContext CapabilityResult.Unavailable
                }
                val capture = IosSpeechAudioCapture(session)
                var task: SFSpeechRecognitionTask? = null
                try {
                    when (val startResult = audioOperation { capture.prepare() }) {
                        is CapabilityResult.Success -> Unit
                        is CapabilityResult.Failed -> return@withContext startResult
                        CapabilityResult.Denied -> return@withContext CapabilityResult.Denied
                        CapabilityResult.Unavailable -> return@withContext CapabilityResult.Unavailable
                    }
                    withTimeoutOrNull(20_000L.milliseconds) {
                        coroutineScope {
                            val outcome =
                                CompletableDeferred<CapabilityResult<List<String>>>(coroutineContext[Job])
                            var lastPartial: String? = null
                            var speechRevision = 0
                            try {
                                // Attach the recognition consumer before the engine emits PCM buffers.
                                task =
                                    recognizer.recognitionTaskWithRequest(capture.request) { result, error ->
                                        if (outcome.isActive) {
                                            if (result != null) {
                                                val text = result.bestTranscription.formattedString
                                                if (text != lastPartial) {
                                                    lastPartial = text
                                                    speechRevision++
                                                }
                                                when {
                                                    result.final -> {
                                                        outcome.complete(
                                                            CapabilityResult.Success(
                                                                listOf(text),
                                                            ),
                                                        )
                                                    }

                                                    error != null -> {
                                                        outcome.complete(
                                                            speechFailure(
                                                                "recognition",
                                                                error,
                                                            ),
                                                        )
                                                    }

                                                    else -> {
                                                        onPartial(listOf(text))
                                                    }
                                                }
                                            } else if (error != null) {
                                                outcome.complete(speechFailure("recognition", error))
                                            }
                                        }
                                    }
                                if (outcome.isCompleted) return@coroutineScope outcome.await()
                                when (val engineResult = audioOperation { capture.startEngine() }) {
                                    is CapabilityResult.Success -> if (outcome.isActive) onReady()
                                    is CapabilityResult.Failed -> outcome.complete(engineResult)
                                    CapabilityResult.Denied -> outcome.complete(CapabilityResult.Denied)
                                    CapabilityResult.Unavailable -> outcome.complete(CapabilityResult.Unavailable)
                                }
                                val silenceWatcher =
                                    launch {
                                        var previousRevision = -1
                                        var elapsed = 0
                                        while (outcome.isActive) {
                                            delay(2500L.milliseconds)
                                            elapsed += 2500
                                            if ((lastPartial != null && previousRevision == speechRevision) || elapsed >= 12_000) {
                                                audioOperation { capture.endAudio() }
                                                // endAudio closes the request's producer; finish asks the
                                                // recognition task to process it into a final result.
                                                // Keep callbacks attached until that result or the deadline.
                                                if (outcome.isActive) task.finish()
                                                break
                                            }
                                            previousRevision = speechRevision
                                        }
                                    }
                                try {
                                    outcome.await()
                                } finally {
                                    silenceWatcher.cancel()
                                }
                            } finally {
                                outcome.cancel()
                            }
                        }
                    } ?: run {
                        AppLogger.warning("KBBI-Speech", "stage=timeout")
                        if (capture.hasAudioBuffers) {
                            CapabilityResult.Failed(FailureReason.SPEECH_EMPTY)
                        } else {
                            CapabilityResult.Unavailable
                        }
                    }
                } finally {
                    // Cancellation must wait for audio teardown before another session starts.
                    withContext(NonCancellable) {
                        task?.cancel()
                        audioOperation { capture.close() }
                    }
                }
            } finally {
                busy = false
            }
        }
}

/** Accessed only from IosSpeechInput's serial audio dispatcher, apart from PCM appends. */
private class IosSpeechAudioCapture(
    private val session: AVAudioSession,
) {
    val request =
        SFSpeechAudioBufferRecognitionRequest().apply { shouldReportPartialResults = true }
    private var engine: AVAudioEngine? = null
    private var active = false
    private var configured = false
    private var tapInstalled = false
    private var engineStarted = false
    private var audioEnded = false
    private var closed = false
    private val bufferCount = AtomicInt(0)
    private val signalDetected = AtomicBoolean(false)
    val hasAudioBuffers: Boolean get() = bufferCount.load() > 0
    private val previousCategory = session.category
    private val previousMode = session.mode
    private val previousOptions = session.categoryOptions

    fun prepare(): CapabilityResult<Unit> {
        if (!session.inputAvailable) {
            AppLogger.warning("KBBI-Speech", "stage=input unavailable")
            return CapabilityResult.Unavailable
        }
        // Keep the output route available while capturing input (including Simulator audio).
        configured =
            speechAudioCall("configure") { error ->
                session.setCategory(
                    AVAudioSessionCategoryPlayAndRecord,
                    mode = AVAudioSessionModeMeasurement,
                    options = AVAudioSessionCategoryOptionMixWithOthers,
                    error = error,
                )
            }
        if (!configured) return CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
        active = speechAudioCall("activate") { error -> session.setActive(true, error = error) }
        if (!active) return CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
        val audioEngine = AVAudioEngine()
        engine = audioEngine
        val input = audioEngine.inputNode
        val format = input.outputFormatForBus(0u)
        if (format.channelCount == 0u || format.sampleRate <= 0.0) {
            AppLogger.warning("KBBI-Speech", "stage=input-format unavailable")
            return CapabilityResult.Unavailable
        }
        AppLogger.debug(
            "KBBI-Speech",
            "stage=input-format sampleRate=${format.sampleRate} channels=${format.channelCount}",
        )
        input.installTapOnBus(0u, bufferSize = 1024u, format = format) { buffer, _ ->
            if (buffer != null && buffer.frameLength > 0u) {
                // No audio/transcript logging or allocation on the real-time callback.
                bufferCount.fetchAndAdd(1)
                if (!signalDetected.load()) {
                    val samples = buffer.floatChannelData?.get(0)
                    if (samples != null) {
                        for (index in 0 until buffer.frameLength.toInt()) {
                            if (abs(samples[index]) > 0.00001f) {
                                signalDetected.store(true)
                                break
                            }
                        }
                    }
                }
                request.appendAudioPCMBuffer(buffer)
            }
        }
        tapInstalled = true
        return CapabilityResult.Success(Unit)
    }

    fun startEngine(): CapabilityResult<Unit> {
        val audioEngine = engine ?: return CapabilityResult.Unavailable
        audioEngine.prepare()
        engineStarted =
            speechAudioCall("engine-start") { error -> audioEngine.startAndReturnError(error) }
        if (engineStarted) AppLogger.debug("KBBI-Speech", "stage=engine-start running")
        return if (engineStarted) {
            CapabilityResult.Success(Unit)
        } else {
            CapabilityResult.Failed(
                FailureReason.PLATFORM_ERROR,
            )
        }
    }

    fun endAudio() {
        if (audioEnded) return
        audioEnded = true
        if (engineStarted) {
            engineStarted = false
            engine?.stop()
        }
        request.endAudio()
        AppLogger.debug(
            "KBBI-Speech",
            "stage=end-audio buffers=${bufferCount.load()} signalDetected=${signalDetected.load()}",
        )
    }

    fun close() {
        if (closed) return
        closed = true
        try {
            endAudio()
            if (tapInstalled) {
                tapInstalled = false
                engine?.inputNode?.removeTapOnBus(0u)
            }
            engine?.reset()
            engine = null
        } finally {
            if (active) {
                active = false
                speechAudioCall("deactivate") { error ->
                    session.setActive(
                        false,
                        withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
                        error = error,
                    )
                }
            }
            if (configured) {
                configured = false
                speechAudioCall("restore-category") { error ->
                    session.setCategory(
                        previousCategory,
                        mode = previousMode,
                        options = previousOptions,
                        error = error,
                    )
                }
            }
        }
    }
}

/** Domain/code only: NSError descriptions and userInfo may contain private content. */
private fun speechFailure(
    stage: String,
    error: NSError,
): CapabilityResult<Nothing> {
    val underlying = error.userInfo[NSUnderlyingErrorKey] as? NSError
    val underlyingCode =
        underlying?.let { " underlyingDomain=${it.domain} underlyingCode=${it.code}" }.orEmpty()
    AppLogger.warning(
        "KBBI-Speech",
        "stage=$stage domain=${error.domain} code=${error.code}$underlyingCode",
    )
    return when {
        error.domain == "kAFAssistantErrorDomain" && error.code == 1110L -> {
            CapabilityResult.Failed(
                FailureReason.SPEECH_EMPTY,
            )
        }

        error.domain == "kAFAssistantErrorDomain" && error.code == 1100L -> {
            CapabilityResult.Failed(
                FailureReason.SPEECH_BUSY,
            )
        }

        error.domain == "kAFAssistantErrorDomain" && error.code == 1700L -> {
            CapabilityResult.Denied
        }

        error.domain == "kAFAssistantErrorDomain" && error.code in
            listOf(
                1101L,
                1107L,
            )
        -> {
            CapabilityResult.Unavailable
        }

        error.domain == "kLSRErrorDomain" && error.code in
            listOf(
                102L,
                201L,
                300L,
            )
        -> {
            CapabilityResult.Unavailable
        }

        else -> {
            CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
        }
    }
}

private fun speechAudioCall(
    stage: String,
    operation: (CPointer<ObjCObjectVar<NSError?>>) -> Boolean,
): Boolean =
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        error.value = null
        val success = operation(error.ptr)
        if (!success) {
            error.value?.let { speechFailure(stage, it) } ?: AppLogger.warning(
                "KBBI-Speech",
                "stage=$stage failed without NSError",
            )
        }
        success
    }
