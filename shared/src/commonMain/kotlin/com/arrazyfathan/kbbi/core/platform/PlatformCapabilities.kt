package com.arrazyfathan.kbbi.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

sealed interface CapabilityResult<out T> {
    data class Success<T>(val value: T) : CapabilityResult<T>

    data object Denied : CapabilityResult<Nothing>

    data object Unavailable : CapabilityResult<Nothing>

    data class Failed(val reason: FailureReason = FailureReason.UNKNOWN) : CapabilityResult<Nothing>
}

enum class FailureReason {
    INVALID_INPUT,
    PLATFORM_ERROR,
    UNKNOWN,
}

fun interface TextShare {
    fun share(text: String): CapabilityResult<Unit>
}

fun interface UrlOpener {
    fun open(url: String): CapabilityResult<Unit>
}

fun interface LocaleProvider {
    fun currentLocaleTag(): String
}

enum class HapticFeedback {
    TAP,
    CONFIRM,
    ERROR,
}

fun interface Haptics {
    fun perform(feedback: HapticFeedback): CapabilityResult<Unit>
}

fun interface SpeechInput {
    suspend fun recognize(): CapabilityResult<String>
}

fun interface NotificationPermission {
    suspend fun request(): CapabilityResult<Unit>
}

enum class AppIconVariant(val storageKey: String) {
    DEFAULT("default"),
    ROYAL_OCEAN("royal_ocean"),
    GOLDEN_SUNSET("golden_sunset"),
    GOLDEN_CORAL_ENERGY("golden_coral_energy"),
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
)

fun interface IncomingLaunchRequests {
    fun observe(): Flow<IncomingLaunchRequest>
}

object UnsupportedSpeechInput : SpeechInput {
    override suspend fun recognize(): CapabilityResult<String> = CapabilityResult.Unavailable
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
