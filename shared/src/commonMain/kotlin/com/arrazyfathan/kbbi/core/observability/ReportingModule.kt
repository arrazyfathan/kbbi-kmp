package com.arrazyfathan.kbbi.core.observability

import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.shared.BuildKonfig
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.dsl.module

class SettingsReportingPreferencesRepository(
    private val settings: SettingsPreferenceStore,
    private val onChanged: () -> Unit,
) : ReportingPreferencesRepository {
    override val preferences =
        settings.preferences.map { it.toReportingPreferences() }.distinctUntilChanged()

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        settings.setCrashReporting(enabled)
        onChanged()
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        settings.setAnalytics(enabled)
        onChanged()
    }

    override suspend fun setPerformanceMonitoringEnabled(enabled: Boolean) {
        settings.setPerformanceMonitoring(enabled)
        onChanged()
    }
}

internal fun com.arrazyfathan.kbbi.feature.settings.domain.SettingsPreferences.toReportingPreferences() =
    ReportingPreferences(crashReportingEnabled, analyticsEnabled, performanceMonitoringEnabled)

val observabilityModule =
    module {
        single<ReportingBackend> { createReportingBackend() }
        single { SettingsReportingPreferencesRepository(get()) { get<ReportingController>().synchronizeCollection() } }
        single<ReportingPreferencesRepository> { get<SettingsReportingPreferencesRepository>() }
        single {
            ReportingController(
                get(),
                { get<SettingsPreferenceStore>().preferences.value.toReportingPreferences() },
                BuildKonfig.BASE_URL,
            )
        }
        single<AnalyticsReporter> { get<ReportingController>() }
        single<CrashReporter> { get<ReportingController>() }
        single<NetworkPerformanceReporter> { get<ReportingController>() }
        single<ReportingCoordinator> {
            PreferencesReportingCoordinator(
                get<ReportingPreferencesRepository>().preferences,
                get(),
            )
        }
    }
