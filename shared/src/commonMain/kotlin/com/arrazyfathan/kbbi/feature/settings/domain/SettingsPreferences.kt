package com.arrazyfathan.kbbi.feature.settings.domain

import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout

enum class AppLanguage(
    val tag: String,
) {
    INDONESIAN("id"),
    ENGLISH("en"),
    ;

    companion object {
        fun fromLanguageTag(tag: String?): AppLanguage =
            if (tag?.substringBefore('-')?.lowercase() == "id" || tag
                    ?.substringBefore('-')
                    ?.lowercase() == "in"
            ) {
                INDONESIAN
            } else {
                ENGLISH
            }
    }
}

enum class ReminderType(
    val key: String,
    val defaultHour: Int,
) {
    DAILY_WORD("daily_word", 9),
    DAILY_PROVERB(
        "daily_proverb",
        12,
    ),
    BOOKMARK_REVIEW("bookmark_review", 19),
}

enum class AppIcon(
    val identifier: String,
) {
    DEFAULT("default"),
    ROYAL_OCEAN("royal_ocean"),
    GOLDEN_SUNSET("golden_sunset"),
    GOLDEN_CORAL_ENERGY(
        "golden_coral_energy",
    ),
    DEEP_FOREST_ENERGY("deep_forest_energy"),
    NEON_VIOLET("neon_violet"),
    BLAZE_ORANGE("blaze_orange"),
    ;

    companion object {
        fun fromIdentifier(identifier: String?): AppIcon = entries.firstOrNull { it.identifier == identifier } ?: DEFAULT
    }
}

data class ReminderPreference(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
)

data class SettingsPreferences(
    val theme: AppTheme = AppTheme.ROYAL_OCEAN,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val hapticsEnabled: Boolean = true,
    val bookmarkLayout: BookmarkLayout = BookmarkLayout.GRID,
    val appIcon: AppIcon = AppIcon.DEFAULT,
    val crashReportingEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val performanceMonitoringEnabled: Boolean = false,
    val campaignNotificationsEnabled: Boolean = false,
    val updateNotificationsEnabled: Boolean = true,
    val reminders: Map<ReminderType, ReminderPreference> =
        ReminderType.entries.associateWith {
            ReminderPreference(hour = it.defaultHour)
        },
)
