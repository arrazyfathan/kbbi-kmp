package com.arrazyfathan.kbbi.core.platform

import java.awt.Desktop
import java.net.URI
import java.util.Locale

internal class DesktopUrlOpener : UrlOpener {
    override fun open(url: String): CapabilityResult<Unit> {
        val uri = runCatching { URI(url) }.getOrNull() ?: return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            return CapabilityResult.Failed(FailureReason.INVALID_INPUT)
        }
        if (!Desktop.isDesktopSupported()) return CapabilityResult.Unavailable

        return runCatching {
            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                CapabilityResult.Unavailable
            } else {
                desktop.browse(uri)
                CapabilityResult.Success(Unit)
            }
        }.getOrElse { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) }
    }
}

internal class DesktopLocaleProvider : LocaleProvider {
    override fun currentLocaleTag(): String = Locale.getDefault().toLanguageTag()
}
