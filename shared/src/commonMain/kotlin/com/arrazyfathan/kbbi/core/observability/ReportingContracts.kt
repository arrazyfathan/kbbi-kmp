package com.arrazyfathan.kbbi.core.observability

import kotlinx.coroutines.flow.Flow

interface AnalyticsReporter {
    fun log(event: AnalyticsEvent)

    fun screenViewed(screen: AnalyticsScreen)
}

object NoOpAnalyticsReporter : AnalyticsReporter {
    override fun log(event: AnalyticsEvent) = Unit

    override fun screenViewed(screen: AnalyticsScreen) = Unit
}

interface NetworkPerformanceReporter {
    fun start(
        url: String,
        method: String,
    ): NetworkPerformanceTrace
}

interface NetworkPerformanceTrace {
    fun response(
        statusCode: Int,
        contentType: String?,
        payloadSizeBytes: Long?,
    )

    fun stop()
}

object NoOpNetworkPerformanceReporter : NetworkPerformanceReporter {
    override fun start(
        url: String,
        method: String,
    ): NetworkPerformanceTrace = NoOpNetworkPerformanceTrace
}

object NoOpNetworkPerformanceTrace : NetworkPerformanceTrace {
    override fun response(
        statusCode: Int,
        contentType: String?,
        payloadSizeBytes: Long?,
    ) = Unit

    override fun stop() = Unit
}

enum class CrashOperation(
    val value: String,
) {
    UnexpectedFailure("unexpected_failure"),
    NetworkRequest("network_request"),
    ResponseDeserialization(
        "response_deserialization",
    ),
    WidgetRefresh("widget_refresh"),
    ReminderDelivery("reminder_delivery"),
    AppIconChange("app_icon_change"),
    UpdateCheck(
        "update_check",
    ),
    UpdateDownload("update_download"),
    UpdateInstall("update_install"),
    LocalDataRead("local_data_read"),
}

interface CrashReporter {
    fun breadcrumb(message: String)

    fun setKey(
        key: String,
        value: String,
    )

    fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String> = emptyMap(),
    )
}

object NoOpCrashReporter : CrashReporter {
    override fun breadcrumb(message: String) = Unit

    override fun setKey(
        key: String,
        value: String,
    ) = Unit

    override fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String>,
    ) = Unit
}

data class ReportingPreferences(
    val crashReportingEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val performanceMonitoringEnabled: Boolean = false,
)

interface ReportingPreferencesRepository {
    val preferences: Flow<ReportingPreferences>

    suspend fun setCrashReportingEnabled(enabled: Boolean)

    suspend fun setAnalyticsEnabled(enabled: Boolean)

    suspend fun setPerformanceMonitoringEnabled(enabled: Boolean)
}

interface ReportingCoordinator {
    /** Runs in the app owner's coroutine; cancellation releases the observer. */
    suspend fun initialize()
}

data class AppBuildInfo(
    val flavor: String,
    val buildType: String,
    val versionName: String,
) {
    val performanceMonitoringEligible: Boolean get() = flavor == "production" && buildType == "release"
}

data class ReportingCapabilities(
    val analytics: Boolean = false,
    val crashes: Boolean = false,
    val performance: Boolean = false,
)

/** Only sanitized, typed payloads cross into the native SDK. */
interface ReportingBackend {
    val capabilities: ReportingCapabilities
    val buildInfo: AppBuildInfo

    fun collection(
        crashes: Boolean,
        analytics: Boolean,
        performance: Boolean,
    )

    fun event(
        name: String,
        parameters: Map<String, String>,
    )

    fun breadcrumb(message: String)

    fun key(
        key: String,
        value: String,
    )

    fun nonFatal(
        operation: String,
        originalType: String,
        context: Map<String, String>,
    )

    fun startNetwork(
        url: String,
        method: String,
    ): NetworkPerformanceTrace
}

class NoOpReportingBackend(
    override val buildInfo: AppBuildInfo = AppBuildInfo("development", "debug", "0.0.0"),
) : ReportingBackend {
    override val capabilities = ReportingCapabilities()

    override fun collection(
        crashes: Boolean,
        analytics: Boolean,
        performance: Boolean,
    ) = Unit

    override fun event(
        name: String,
        parameters: Map<String, String>,
    ) = Unit

    override fun breadcrumb(message: String) = Unit

    override fun key(
        key: String,
        value: String,
    ) = Unit

    override fun nonFatal(
        operation: String,
        originalType: String,
        context: Map<String, String>,
    ) = Unit

    override fun startNetwork(
        url: String,
        method: String,
    ) = NoOpNetworkPerformanceTrace
}

internal expect fun createReportingBackend(): ReportingBackend
