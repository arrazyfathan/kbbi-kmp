package com.arrazyfathan.kbbi.feature.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.palette
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.app_icon_blaze_orange
import kbbi_kmp.shared.generated.resources.app_icon_deep_forest_energy
import kbbi_kmp.shared.generated.resources.app_icon_default
import kbbi_kmp.shared.generated.resources.app_icon_golden_coral_energy
import kbbi_kmp.shared.generated.resources.app_icon_golden_sunset
import kbbi_kmp.shared.generated.resources.app_icon_neon_violet
import kbbi_kmp.shared.generated.resources.app_icon_picker_description
import kbbi_kmp.shared.generated.resources.app_icon_picker_title
import kbbi_kmp.shared.generated.resources.app_icon_preview_accessibility
import kbbi_kmp.shared.generated.resources.app_icon_preview_blaze_orange
import kbbi_kmp.shared.generated.resources.app_icon_preview_deep_forest_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_default
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_coral_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_sunset
import kbbi_kmp.shared.generated.resources.app_icon_preview_neon_violet
import kbbi_kmp.shared.generated.resources.app_icon_preview_royal_ocean
import kbbi_kmp.shared.generated.resources.app_icon_royal_ocean
import kbbi_kmp.shared.generated.resources.appearance_section_title
import kbbi_kmp.shared.generated.resources.ic_check
import kbbi_kmp.shared.generated.resources.theme_blaze_orange
import kbbi_kmp.shared.generated.resources.theme_deep_forest_energy
import kbbi_kmp.shared.generated.resources.theme_golden_coral_energy
import kbbi_kmp.shared.generated.resources.theme_golden_sunset
import kbbi_kmp.shared.generated.resources.theme_neon_violet
import kbbi_kmp.shared.generated.resources.theme_picker_description
import kbbi_kmp.shared.generated.resources.theme_royal_ocean
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val APP_ICON_COLUMN_COUNT = 6
private const val APP_ICON_OPTION_TEST_TAG_PREFIX = "app_icon_option_"
private const val APP_ICON_SELECTED_INDICATOR_TEST_TAG_PREFIX = "app_icon_selected_indicator_"

@Composable
internal fun AppearanceSection(
    selectedTheme: AppTheme,
    selectedAppIcon: AppIcon,
    onThemeSelected: (AppTheme) -> Unit,
    onAppIconSelected: (AppIcon) -> Unit,
) {
    Section(title = stringResource(Res.string.appearance_section_title)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.theme_picker_description),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppTheme.entries.chunked(2).forEach { themes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        themes.forEach { theme ->
                            ThemeOption(
                                theme = theme,
                                selected = theme == selectedTheme,
                                onClick = { onThemeSelected(theme) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (themes.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = stringResource(Res.string.app_icon_picker_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = stringResource(Res.string.app_icon_picker_description),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppIcon.entries.chunked(APP_ICON_COLUMN_COUNT).forEach { icons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        icons.forEach { icon ->
                            AppIconOption(
                                icon = icon,
                                selected = icon == selectedAppIcon,
                                onClick = { onAppIconSelected(icon) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(APP_ICON_COLUMN_COUNT - icons.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconOption(
    icon: AppIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(icon.labelResId)
    val shape = RoundedCornerShape(100.dp)
    val accessibilityDescription = stringResource(Res.string.app_icon_preview_accessibility, label)
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .testTag("$APP_ICON_OPTION_TEST_TAG_PREFIX${icon.identifier}")
                .clip(shape)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    shape = shape,
                ).selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon.previewResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (selected) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .testTag("$APP_ICON_SELECTED_INDICATOR_TEST_TAG_PREFIX${icon.identifier}")
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

private val AppIcon.labelResId: StringResource
    get() =
        when (this) {
            AppIcon.DEFAULT -> Res.string.app_icon_default
            AppIcon.ROYAL_OCEAN -> Res.string.app_icon_royal_ocean
            AppIcon.GOLDEN_SUNSET -> Res.string.app_icon_golden_sunset
            AppIcon.GOLDEN_CORAL_ENERGY -> Res.string.app_icon_golden_coral_energy
            AppIcon.DEEP_FOREST_ENERGY -> Res.string.app_icon_deep_forest_energy
            AppIcon.NEON_VIOLET -> Res.string.app_icon_neon_violet
            AppIcon.BLAZE_ORANGE -> Res.string.app_icon_blaze_orange
        }

private val AppIcon.previewResId: DrawableResource
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

@Composable
private fun ThemeOption(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = theme.palette
    Card(
        modifier =
            modifier.selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    Box(
                        modifier = Modifier.size(28.dp).background(palette.primary, CircleShape),
                    )
                    Box(
                        modifier = Modifier.offset(x = 18.dp).size(28.dp).background(palette.secondary, CircleShape),
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = stringResource(theme.labelResId),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
            )
        }
    }
}

private val AppTheme.labelResId: StringResource
    get() =
        when (this) {
            AppTheme.ROYAL_OCEAN -> Res.string.theme_royal_ocean
            AppTheme.GOLDEN_SUNSET -> Res.string.theme_golden_sunset
            AppTheme.GOLDEN_CORAL_ENERGY -> Res.string.theme_golden_coral_energy
            AppTheme.DEEP_FOREST_ENERGY -> Res.string.theme_deep_forest_energy
            AppTheme.NEON_VIOLET -> Res.string.theme_neon_violet
            AppTheme.BLAZE_ORANGE -> Res.string.theme_blaze_orange
        }
