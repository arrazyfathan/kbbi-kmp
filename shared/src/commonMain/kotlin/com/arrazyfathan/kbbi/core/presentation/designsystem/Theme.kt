package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.arrazyfathan.kbbi.core.domain.model.AppTheme

internal fun colorSchemeFor(theme: AppTheme) =
    with(theme.palette) {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            background = BlueBg,
            surface = Color.White,
            surfaceTint = Color.Transparent,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            outlineVariant = BlueBg,
        )
    }

@Composable
fun KBBITheme(
    theme: AppTheme = AppTheme.ROYAL_OCEAN,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(theme),
        typography = KBBITypography,
        content = content,
    )
}
