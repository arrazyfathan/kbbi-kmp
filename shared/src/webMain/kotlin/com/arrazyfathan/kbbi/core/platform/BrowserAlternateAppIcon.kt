package com.arrazyfathan.kbbi.core.platform

import web.dom.document
import web.storage.localStorage

internal class BrowserAlternateAppIcon : AlternateAppIcon {
    override val target: AppIconTarget = AppIconTarget.BROWSER_TAB

    override suspend fun currentIcon(): CapabilityResult<AppIconVariant> =
        runCatching {
            AppIconVariant.entries.firstOrNull { it.storageKey == localStorage.getItem(STORAGE_KEY) }
                ?: AppIconVariant.DEFAULT
        }.fold(
            onSuccess = { CapabilityResult.Success(it) },
            onFailure = { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) },
        )

    override suspend fun setIcon(variant: AppIconVariant): CapabilityResult<AppIconVariant> =
        runCatching {
            val href = "icon-${variant.storageKey}.png"
            val iconLink =
                document.querySelector("link[rel~='icon']") ?: document.createElement("link").also {
                    it.setAttribute("rel", "icon")
                    document.head.appendChild(it)
                }
            iconLink.setAttribute("href", href)
            localStorage.setItem(STORAGE_KEY, variant.storageKey)
            variant
        }.fold(
            onSuccess = { CapabilityResult.Success(it) },
            onFailure = { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) },
        )

    private companion object {
        const val STORAGE_KEY = "kbbi.app_icon"
    }
}
