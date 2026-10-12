package com.arrazyfathan.kbbi.feature.settings.data

import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.feature.bookmark.data.BookmarkLayoutPreferenceStore
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderPreference
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal expect fun readSettingsPreference(key: String): String?

internal expect fun writeSettingsPreference(
    key: String,
    value: String,
)

internal expect fun applySettingsLanguage(languageTag: String)

class SettingsPreferenceStore(
    private val bookmarkLayoutStore: BookmarkLayoutPreferenceStore,
    private val localeProvider: LocaleProvider,
) {
    private val mutablePreferences = MutableStateFlow(readPreferences())
    val preferences: StateFlow<SettingsPreferences> = mutablePreferences.asStateFlow()

    init {
        applySettingsLanguage(mutablePreferences.value.language.tag)
    }

    fun setTheme(value: AppTheme) = update("theme", value.storageKey) { copy(theme = value) }

    fun setLanguage(value: AppLanguage) {
        writeSettingsPreference("language", value.tag)
        applySettingsLanguage(value.tag)
        mutablePreferences.value = mutablePreferences.value.copy(language = value)
    }

    fun setHaptics(value: Boolean) = update("haptics", value.toString()) { copy(hapticsEnabled = value) }

    fun setBookmarkLayout(value: BookmarkLayout) {
        bookmarkLayoutStore.setLayout(value)
        update("bookmark_layout", value.storageKey) { copy(bookmarkLayout = value) }
    }

    fun setAppIcon(value: AppIcon) = update("app_icon", value.identifier) { copy(appIcon = value) }

    fun setCrashReporting(value: Boolean) = update("crash_reporting", value.toString()) { copy(crashReportingEnabled = value) }

    fun setAnalytics(value: Boolean) = update("analytics", value.toString()) { copy(analyticsEnabled = value) }

    fun setPerformanceMonitoring(value: Boolean) =
        update(
            "performance_monitoring",
            value.toString(),
        ) { copy(performanceMonitoringEnabled = value) }

    fun setCampaignNotifications(value: Boolean) =
        update(
            "campaign_notifications",
            value.toString(),
        ) { copy(campaignNotificationsEnabled = value) }

    fun setUpdateNotifications(value: Boolean) =
        update(
            "update_notifications",
            value.toString(),
        ) { copy(updateNotificationsEnabled = value) }

    fun setReminder(
        type: ReminderType,
        value: ReminderPreference,
    ) {
        writeSettingsPreference("${type.key}_enabled", value.enabled.toString())
        writeSettingsPreference("${type.key}_hour", value.hour.toString())
        writeSettingsPreference("${type.key}_minute", value.minute.toString())
        mutablePreferences.value =
            mutablePreferences.value.copy(reminders = mutablePreferences.value.reminders + (type to value))
    }

    private fun update(
        key: String,
        value: String,
        transform: SettingsPreferences.() -> SettingsPreferences,
    ) {
        writeSettingsPreference(key, value)
        mutablePreferences.value = mutablePreferences.value.transform()
    }

    private fun readPreferences() =
        SettingsPreferences(
            theme = AppTheme.fromStorageKey(readSettingsPreference("theme")),
            language =
                AppLanguage.fromLanguageTag(
                    readSettingsPreference("language") ?: localeProvider.currentLocaleTag(),
                ),
            hapticsEnabled = readSettingsPreference("haptics")?.toBooleanStrictOrNull() ?: true,
            bookmarkLayout = BookmarkLayout.fromStorageKey(readSettingsPreference("bookmark_layout")),
            appIcon = AppIcon.fromIdentifier(readSettingsPreference("app_icon")),
            crashReportingEnabled =
                readSettingsPreference("crash_reporting")?.toBooleanStrictOrNull()
                    ?: true,
            analyticsEnabled = readSettingsPreference("analytics")?.toBooleanStrictOrNull() ?: false,
            performanceMonitoringEnabled =
                readSettingsPreference("performance_monitoring")?.toBooleanStrictOrNull()
                    ?: false,
            campaignNotificationsEnabled =
                readSettingsPreference("campaign_notifications")?.toBooleanStrictOrNull()
                    ?: false,
            updateNotificationsEnabled =
                readSettingsPreference("update_notifications")?.toBooleanStrictOrNull()
                    ?: true,
            reminders =
                ReminderType.entries.associateWith { type ->
                    ReminderPreference(
                        enabled =
                            readSettingsPreference("${type.key}_enabled")?.toBooleanStrictOrNull()
                                ?: false,
                        hour =
                            readSettingsPreference("${type.key}_hour")?.toIntOrNull()?.coerceIn(0, 23)
                                ?: type.defaultHour,
                        minute =
                            readSettingsPreference("${type.key}_minute")
                                ?.toIntOrNull()
                                ?.coerceIn(0, 59) ?: 0,
                    )
                },
        )
}
