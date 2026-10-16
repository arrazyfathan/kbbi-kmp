@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.arrazyfathan.kbbi.core.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import platform.Foundation.NSNumber
import platform.Foundation.NSSelectorFromString
import platform.Foundation.valueForKey
import platform.UIKit.UIApplication
import kotlin.time.Duration.Companion.milliseconds

internal class IosAlternateAppIcon : AlternateAppIcon {
    override val target: AppIconTarget = AppIconTarget.IOS_HOME_SCREEN

    override suspend fun currentIcon(): CapabilityResult<AppIconVariant> =
        withContext(Dispatchers.Main.immediate) {
            runCatching {
                val application = UIApplication.sharedApplication
                if (!application.supportsAlternateIcons()) return@withContext CapabilityResult.Unavailable
                val name = application.alternateIconName()
                CapabilityResult.Success(
                    AppIconVariant.entries.firstOrNull { it.iosAssetName == name }
                        ?: AppIconVariant.DEFAULT,
                )
            }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
        }

    override suspend fun setIcon(variant: AppIconVariant): CapabilityResult<AppIconVariant> =
        withContext(Dispatchers.Main.immediate) {
            val application = UIApplication.sharedApplication
            if (!application.supportsAlternateIcons()) return@withContext CapabilityResult.Unavailable
            runCatching {
                // These UIKit category methods are omitted from this Kotlin/Native binding.
                application.performSelector(
                    NSSelectorFromString("setAlternateIconName:completionHandler:"),
                    variant.iosAssetName,
                    null,
                )
            }.getOrElse { return@withContext CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
            // UIKit presents its own confirmation prompt and reports the applied name through
            // alternateIconName. Poll that authoritative value so rejection never saves a
            // false selection. Delay keeps this operation cancellable with the Settings screen.
            repeat(300) {
                if (application.alternateIconName() == variant.iosAssetName) {
                    return@withContext CapabilityResult.Success(variant)
                }
                delay(100.milliseconds)
            }
            CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
        }

    private val AppIconVariant.iosAssetName: String?
        get() =
            when (this) {
                AppIconVariant.DEFAULT -> null
                AppIconVariant.ROYAL_OCEAN -> "AppIconRoyalOcean"
                AppIconVariant.GOLDEN_SUNSET -> "AppIconGoldenSunset"
                AppIconVariant.GOLDEN_CORAL_ENERGY -> "AppIconGoldenCoralEnergy"
                AppIconVariant.DEEP_FOREST_ENERGY -> "AppIconDeepForestEnergy"
                AppIconVariant.NEON_VIOLET -> "AppIconNeonViolet"
                AppIconVariant.BLAZE_ORANGE -> "AppIconBlazeOrange"
            }

    private fun UIApplication.supportsAlternateIcons(): Boolean = (valueForKey("supportsAlternateIcons") as? NSNumber)?.boolValue == true

    private fun UIApplication.alternateIconName(): String? = valueForKey("alternateIconName") as? String
}
