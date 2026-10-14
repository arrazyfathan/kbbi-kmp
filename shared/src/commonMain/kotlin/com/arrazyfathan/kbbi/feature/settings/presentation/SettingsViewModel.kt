package com.arrazyfathan.kbbi.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.observability.ReportingCapabilities
import com.arrazyfathan.kbbi.core.observability.ReportingController
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.SettingsPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout as BookmarkLayoutModel
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon as AppIconModel

class SettingsViewModel(
    private val preferences: SettingsPreferenceStore,
    private val clearSearchHistory: ClearSearchHistoryUseCase,
    private val reporting: ReportingController,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val state: StateFlow<SettingsState> =
        preferences.preferences
            .map {
                SettingsState(
                    it,
                    reporting.capabilities,
                    reporting.performanceEligible,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SettingsState(
                    preferences.preferences.value,
                    reporting.capabilities,
                    reporting.performanceEligible,
                ),
            )

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.Theme -> {
                preferences.setTheme(action.theme)
            }

            is SettingsAction.Language -> {
                preferences.setLanguage(action.language)
            }

            is SettingsAction.Haptics -> {
                preferences.setHaptics(action.enabled)
            }

            is SettingsAction.BookmarkLayout -> {
                preferences.setBookmarkLayout(action.layout)
            }

            is SettingsAction.CrashReporting -> {
                preferences.setCrashReporting(action.enabled)
                reporting.synchronizeCollection()
            }

            is SettingsAction.Analytics -> {
                preferences.setAnalytics(action.enabled)
                reporting.synchronizeCollection()
            }

            is SettingsAction.PerformanceMonitoring -> {
                preferences.setPerformanceMonitoring(action.enabled)
                reporting.synchronizeCollection()
            }

            is SettingsAction.Reminder -> {
                val current =
                    preferences.preferences.value.reminders
                        .getValue(action.type)
                preferences.setReminder(action.type, current.copy(enabled = action.enabled))
                viewModelScope.launch {
                    if (action.enabled) {
                        reminderScheduler.schedule(action.type)
                    } else {
                        reminderScheduler.cancel(
                            action.type,
                        )
                    }
                }
            }

            is SettingsAction.ReminderTimeChanged -> {
                val current =
                    preferences.preferences.value.reminders
                        .getValue(action.type)
                preferences.setReminder(
                    action.type,
                    current.copy(hour = action.hour, minute = action.minute),
                )
                viewModelScope.launch {
                    if (current.enabled) reminderScheduler.schedule(action.type)
                }
            }

            is SettingsAction.AppIcon -> {
                preferences.setAppIcon(action.icon)
            }

            is SettingsAction.CampaignNotifications -> {
                preferences.setCampaignNotifications(action.enabled)
            }

            is SettingsAction.UpdateNotifications -> {
                preferences.setUpdateNotifications(action.enabled)
            }

            is SettingsAction.ClearHistory -> {
                viewModelScope.launch { clearSearchHistory() }
            }
        }
    }
}

data class SettingsState(
    val preferences: SettingsPreferences,
    val reportingCapabilities: ReportingCapabilities = ReportingCapabilities(),
    val performanceMonitoringEligible: Boolean = false,
)

sealed interface SettingsAction {
    data class Theme(
        val theme: AppTheme,
    ) : SettingsAction

    data class Language(
        val language: AppLanguage,
    ) : SettingsAction

    data class Haptics(
        val enabled: Boolean,
    ) : SettingsAction

    data class BookmarkLayout(
        val layout: BookmarkLayoutModel,
    ) : SettingsAction

    data class CrashReporting(
        val enabled: Boolean,
    ) : SettingsAction

    data class Analytics(
        val enabled: Boolean,
    ) : SettingsAction

    data class PerformanceMonitoring(
        val enabled: Boolean,
    ) : SettingsAction

    data class Reminder(
        val type: ReminderType,
        val enabled: Boolean,
    ) : SettingsAction

    data class ReminderTimeChanged(
        val type: ReminderType,
        val hour: Int,
        val minute: Int,
    ) : SettingsAction

    data class AppIcon(
        val icon: AppIconModel,
    ) : SettingsAction

    data class CampaignNotifications(
        val enabled: Boolean,
    ) : SettingsAction

    data class UpdateNotifications(
        val enabled: Boolean,
    ) : SettingsAction

    data object ClearHistory : SettingsAction
}
