package com.arrazyfathan.kbbi.core.platform

import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UIModalPresentationFullScreen

internal class IosTextShare : TextShare {
    override fun share(text: String): CapabilityResult<Unit> =
        runCatching {
            val rootViewController =
                UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return CapabilityResult.Unavailable
            val shareController = UIActivityViewController(listOf(text), null)
            shareController.modalPresentationStyle = UIModalPresentationFullScreen
            rootViewController.presentViewController(shareController, animated = true, completion = null)
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}

internal class IosUrlOpener : UrlOpener {
    override fun open(url: String): CapabilityResult<Unit> {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }
        val nativeUrl = NSURL.URLWithString(url) ?: return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        return if (UIApplication.sharedApplication.openURL(nativeUrl)) {
            CapabilityResult.Success(Unit)
        } else {
            CapabilityResult.Unavailable
        }
    }
}

internal class IosLocaleProvider : LocaleProvider {
    override fun currentLocaleTag(): String =
        (NSUserDefaults.standardUserDefaults.arrayForKey("AppleLanguages")?.firstOrNull() as? String ?: "en")
            .replace('_', '-')
}

internal class IosHaptics : Haptics {
    override fun perform(feedback: HapticFeedback): CapabilityResult<Unit> =
        runCatching {
            val generator =
                UIImpactFeedbackGenerator(
                    when (feedback) {
                        HapticFeedback.TAP -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
                        HapticFeedback.CONFIRM -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
                        HapticFeedback.ERROR -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
                    },
                )
            generator.prepare()
            generator.impactOccurred()
            CapabilityResult.Success(Unit)
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
}
