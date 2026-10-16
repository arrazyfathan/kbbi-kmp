package com.arrazyfathan.kbbi.core.platform

/** The Desktop host applies the saved selection to its Compose Window icon. */
internal object DesktopAlternateAppIcon : AlternateAppIcon {
    override val target: AppIconTarget = AppIconTarget.DESKTOP_WINDOW

    override suspend fun currentIcon(): CapabilityResult<AppIconVariant> = CapabilityResult.Success(AppIconVariant.DEFAULT)

    override suspend fun setIcon(variant: AppIconVariant): CapabilityResult<AppIconVariant> = CapabilityResult.Success(variant)
}
