package com.arrazyfathan.kbbi.core.platform

import web.navigator.navigator
import web.window.window

internal class BrowserUrlOpener : UrlOpener {
    override fun open(url: String): CapabilityResult<Unit> {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }
        return runCatching {
            if (window.open(url) == null) {
                CapabilityResult.Denied
            } else {
                CapabilityResult.Success(Unit)
            }
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
    }
}

internal class BrowserLocaleProvider : LocaleProvider {
    override fun currentLocaleTag(): String = navigator.language
}
