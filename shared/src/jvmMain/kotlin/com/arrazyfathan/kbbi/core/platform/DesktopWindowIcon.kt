package com.arrazyfathan.kbbi.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.app_icon_preview_blaze_orange
import kbbi_kmp.shared.generated.resources.app_icon_preview_deep_forest_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_default
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_coral_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_sunset
import kbbi_kmp.shared.generated.resources.app_icon_preview_neon_violet
import kbbi_kmp.shared.generated.resources.app_icon_preview_royal_ocean
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@Composable
fun DesktopWindowIcon(): Painter {
    val settings: SettingsPreferenceStore = koinInject()
    val preferences by settings.preferences.collectAsState()
    return painterResource(preferences.appIcon.windowIcon)
}

private val AppIcon.windowIcon: DrawableResource
    get() =
        when (this) {
            AppIcon.DEFAULT -> Res.drawable.app_icon_preview_default
            AppIcon.ROYAL_OCEAN -> Res.drawable.app_icon_preview_royal_ocean
            AppIcon.GOLDEN_SUNSET -> Res.drawable.app_icon_preview_golden_sunset
            AppIcon.GOLDEN_CORAL_ENERGY -> Res.drawable.app_icon_preview_golden_coral_energy
            AppIcon.DEEP_FOREST_ENERGY -> Res.drawable.app_icon_preview_deep_forest_energy
            AppIcon.NEON_VIOLET -> Res.drawable.app_icon_preview_neon_violet
            AppIcon.BLAZE_ORANGE -> Res.drawable.app_icon_preview_blaze_orange
        }
