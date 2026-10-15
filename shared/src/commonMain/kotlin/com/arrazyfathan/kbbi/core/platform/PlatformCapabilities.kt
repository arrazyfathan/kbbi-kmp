package com.arrazyfathan.kbbi.core.platform

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface CapabilityResult<out T> {
    data class Success<T>(
        val value: T,
    ) : CapabilityResult<T>

    data object Denied : CapabilityResult<Nothing>

    data object Unavailable : CapabilityResult<Nothing>

    data class Failed(
        val reason: FailureReason = FailureReason.UNKNOWN,
    ) : CapabilityResult<Nothing>
}

enum class FailureReason {
    INVALID_INPUT,
    PLATFORM_ERROR,
    UNKNOWN,
    SPEECH_EMPTY,
    SPEECH_BUSY,
}

@Immutable
fun interface TextShare {
    fun share(text: String): CapabilityResult<Unit>
}

fun interface UrlOpener {
    /** Opens the URL; success means the request was accepted by the platform. */
    fun open(url: String): CapabilityResult<Unit>

    /** Waits for the platform's result when launching URLs is asynchronous. */
    suspend fun openAndAwait(url: String): CapabilityResult<Unit> = open(url)
}

fun interface LocaleProvider {
    fun currentLocaleTag(): String
}

enum class HapticFeedback {
    TAP,
    LONG_PRESS,
    CONFIRM,
    ERROR,
}

fun interface Haptics {
    fun perform(feedback: HapticFeedback): CapabilityResult<Unit>
}

interface PlatformHaptics : Haptics

class SettingsAwareHaptics(
    private val delegate: PlatformHaptics,
    private val enabled: () -> Boolean,
) : Haptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> =
        if (enabled()) delegate.perform(feedback) else CapabilityResult.Success(Unit)
}

object UnsupportedPlatformHaptics : PlatformHaptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> = CapabilityResult.Unavailable
}

fun interface SpeechInput {
    suspend fun recognize(): CapabilityResult<String>

    /** Session is owned by the calling coroutine; cancellation releases the microphone. */
    suspend fun recognizeCandidates(
        onReady: () -> Unit = {},
        onPartial: (List<String>) -> Unit,
    ): CapabilityResult<List<String>> {
        onReady()
        return when (val result = recognize()) {
            is CapabilityResult.Success -> CapabilityResult.Success(listOf(result.value))
            is CapabilityResult.Failed -> result
            CapabilityResult.Denied -> CapabilityResult.Denied
            CapabilityResult.Unavailable -> CapabilityResult.Unavailable
        }
    }
}

fun interface NotificationPermission {
    suspend fun request(): CapabilityResult<Unit>
}

enum class AppIconVariant(
    val storageKey: String,
) {
    DEFAULT("default"),
    ROYAL_OCEAN("royal_ocean"),
    GOLDEN_SUNSET("golden_sunset"),
    GOLDEN_CORAL_ENERGY(
        "golden_coral_energy",
    ),
    DEEP_FOREST_ENERGY("deep_forest_energy"),
    NEON_VIOLET("neon_violet"),
    BLAZE_ORANGE("blaze_orange"),
}

fun interface AlternateAppIcon {
    fun setIcon(variant: AppIconVariant): CapabilityResult<Unit>
}

data class IncomingLaunchRequest(
    val uri: String? = null,
    val sharedText: String? = null,
    val action: IncomingLaunchAction? = null,
    // Hosts must reuse this identity while redelivering the same OS request.
    val requestId: String =
        kotlin.random.Random
            .nextLong()
            .toString(),
)

/** Semantic actions shared by OS shortcuts, widgets, notifications, and CLI launchers. */
enum class IncomingLaunchAction {
    SEARCH,
    FOCUS_SEARCH,
    RANDOM_WORD,
    OPEN_BOOKMARKS,
    OPEN_PROVERBS,
    OPEN_HOME,
}

fun interface IncomingLaunchRequests {
    fun observe(): Flow<IncomingLaunchRequest>

    /** Hosts remove the acknowledged request from their pending launch queue. */
    fun acknowledge(requestId: String) {}
}

object UnsupportedSpeechInput : SpeechInput {
    override suspend fun recognize(): CapabilityResult<String> = CapabilityResult.Unavailable

    override suspend fun recognizeCandidates(
        onReady: () -> Unit,
        onPartial: (List<String>) -> Unit,
    ): CapabilityResult<List<String>> = CapabilityResult.Unavailable
}

object UnsupportedTextShare : TextShare {
    override fun share(text: String): CapabilityResult<Unit> = CapabilityResult.Unavailable
}

object UnsupportedHaptics : Haptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> = CapabilityResult.Unavailable
}

object UnsupportedNotificationPermission : NotificationPermission {
    override suspend fun request(): CapabilityResult<Unit> = CapabilityResult.Unavailable
}

object UnsupportedAlternateAppIcon : AlternateAppIcon {
    override fun setIcon(variant: AppIconVariant): CapabilityResult<Unit> = CapabilityResult.Unavailable
}

object EmptyIncomingLaunchRequests : IncomingLaunchRequests {
    override fun observe(): Flow<IncomingLaunchRequest> = emptyFlow()
}
