package com.arrazyfathan.kbbi.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.observability.ReportingCapabilities
import com.arrazyfathan.kbbi.core.observability.ReportingController
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.AppIconTarget
import com.arrazyfathan.kbbi.core.platform.AppIconVariant
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.FailureReason
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout as BookmarkLayoutModel
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon as AppIconModel

class SettingsViewModel(
    private val preferences: SettingsPreferenceStore,
    private val clearSearchHistory: ClearSearchHistoryUseCase,
    private val reporting: ReportingController,
    private val reminderScheduler: ReminderScheduler,
    private val alternateAppIcon: AlternateAppIcon,
) : ViewModel() {
    private val appIconUpdating = MutableStateFlow(false)
    private val appIconChangeResult = MutableStateFlow<AppIconChangeStatus?>(null)

    val state: StateFlow<SettingsState> =
        combine(
            preferences.preferences,
            appIconUpdating,
            appIconChangeResult,
        ) { current, updating, result ->
            SettingsState(
                current,
                reporting.capabilities,
                reporting.performanceEligible,
                alternateAppIcon.target,
                updating,
                result,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsState(
                preferences.preferences.value,
                reporting.capabilities,
                reporting.performanceEligible,
                alternateAppIcon.target,
            ),
        )

    init {
        viewModelScope.launch {
            val saved =
                preferences.preferences.value.appIcon
                    .toVariant()
            when (alternateAppIcon.target) {
                AppIconTarget.LAUNCHER, AppIconTarget.IOS_HOME_SCREEN -> {
                    (alternateAppIcon.currentIcon() as? CapabilityResult.Success)?.value?.let { actual ->
                        preferences.setAppIcon(AppIcon.fromIdentifier(actual.storageKey))
                    }
                }

                AppIconTarget.DESKTOP_WINDOW, AppIconTarget.BROWSER_TAB -> {
                    alternateAppIcon.setIcon(
                        saved,
                    )
                }

                AppIconTarget.UNSUPPORTED -> {
                    Unit
                }
            }
        }
    }

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
                if (appIconUpdating.value || action.icon == preferences.preferences.value.appIcon) return
                viewModelScope.launch {
                    appIconUpdating.value = true
                    appIconChangeResult.value = null
                    try {
                        val result =
                            try {
                                alternateAppIcon.setIcon(action.icon.toVariant())
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
                            }
                        when (result) {
                            is CapabilityResult.Success -> {
                                preferences.setAppIcon(AppIcon.fromIdentifier(result.value.storageKey))
                                appIconChangeResult.value = AppIconChangeStatus.SUCCEEDED
                            }

                            CapabilityResult.Denied, CapabilityResult.Unavailable, is CapabilityResult.Failed -> {
                                // Read back native state after an OS rejection; keep the picker truthful.
                                val actual =
                                    try {
                                        alternateAppIcon.currentIcon()
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        null
                                    }
                                if (actual is CapabilityResult.Success) {
                                    preferences.setAppIcon(AppIcon.fromIdentifier(actual.value.storageKey))
                                }
                                appIconChangeResult.value = AppIconChangeStatus.FAILED
                            }
                        }
                    } finally {
                        appIconUpdating.value = false
                    }
                }
            }

            SettingsAction.DismissAppIconMessage -> {
                appIconChangeResult.value = null
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
    val appIconTarget: AppIconTarget = AppIconTarget.UNSUPPORTED,
    val appIconUpdating: Boolean = false,
    val appIconChangeResult: AppIconChangeStatus? = null,
)

enum class AppIconChangeStatus { SUCCEEDED, FAILED }

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

    data object DismissAppIconMessage : SettingsAction

    data class CampaignNotifications(
        val enabled: Boolean,
    ) : SettingsAction

    data class UpdateNotifications(
        val enabled: Boolean,
    ) : SettingsAction

    data object ClearHistory : SettingsAction
}

private fun AppIcon.toVariant(): AppIconVariant = AppIconVariant.entries.first { it.storageKey == identifier }
