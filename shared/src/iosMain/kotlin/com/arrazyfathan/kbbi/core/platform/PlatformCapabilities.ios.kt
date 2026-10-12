package com.arrazyfathan.kbbi.core.platform

import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIModalPresentationFullScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class IosTextShare : TextShare {
    override fun share(text: String): CapabilityResult<Unit> =
        runCatching {
            val rootViewController =
                UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return CapabilityResult.Unavailable
            val shareController = UIActivityViewController(listOf(text), null)
            shareController.modalPresentationStyle = UIModalPresentationFullScreen
            rootViewController.presentViewController(
                shareController,
                animated = true,
                completion = null,
            )
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}

internal class IosUrlOpener : UrlOpener {
    override fun open(url: String): CapabilityResult<Unit> = openNative(url)

    override suspend fun openAndAwait(url: String): CapabilityResult<Unit> =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val dispatchResult = openNative(url) { opened ->
                    if (continuation.isActive) {
                        continuation.resume(
                            if (opened) CapabilityResult.Success(Unit) else CapabilityResult.Unavailable,
                        )
                    }
                }
                if (dispatchResult !is CapabilityResult.Success && continuation.isActive) {
                    continuation.resume(dispatchResult)
                }
            }
        }

    private fun openNative(
        url: String,
        onOpened: ((Boolean) -> Unit)? = null,
    ): CapabilityResult<Unit> {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }
        val nativeUrl =
            NSURL.URLWithString(url) ?: return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        if (nativeUrl.host.isNullOrBlank()) return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        return runCatching {
            val application = UIApplication.sharedApplication
            if (!application.canOpenURL(nativeUrl)) {
                CapabilityResult.Unavailable
            } else {
                application.openURL(nativeUrl, options = emptyMap<Any?, Any?>(), completionHandler = onOpened)
                CapabilityResult.Success(Unit)
            }
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
    }
}

internal class IosLocaleProvider : LocaleProvider {
    override fun currentLocaleTag(): String =
        (
            NSUserDefaults.standardUserDefaults.arrayForKey("AppleLanguages")?.firstOrNull() as? String
                ?: "en"
        ).replace('_', '-')
}

internal class IosHaptics : PlatformHaptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> =
        runCatching {
            val generator =
                UIImpactFeedbackGenerator(
                    when (feedback) {
                        HapticFeedback.TAP -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
                        HapticFeedback.LONG_PRESS -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
                        HapticFeedback.CONFIRM -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
                        HapticFeedback.ERROR -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
                    },
                )
            generator.prepare()
            generator.impactOccurred()
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}
