package com.arrazyfathan.kbbi.core.observability

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** Reads current preferences at each boundary, including while the UI observer is paused. */
class ReportingController(
    private val backend: ReportingBackend,
    private val currentPreferences: () -> ReportingPreferences,
    private val trustedOrigin: String,
) : AnalyticsReporter,
    CrashReporter,
    NetworkPerformanceReporter {
    val capabilities: ReportingCapabilities = backend.capabilities
    val performanceEligible: Boolean = backend.buildInfo.performanceMonitoringEligible
    private val seenScreen = ScreenViewDeduplicator()

    fun synchronizeCollection() {
        val p = currentPreferences()
        runCatching {
            backend.collection(
                p.crashReportingEnabled && capabilities.crashes,
                p.analyticsEnabled && capabilities.analytics,
                p.performanceMonitoringEnabled && performanceEligible && capabilities.performance,
            )
        }
        val b = backend.buildInfo
        setKey("app_flavor", b.flavor)
        setKey("build_type", b.buildType)
        setKey("app_version", b.versionName)
    }

    override fun log(event: AnalyticsEvent) {
        if (currentPreferences().analyticsEnabled && capabilities.analytics) {
            runCatching { backend.event(event.name, event.parameters) }
        }
    }

    override fun screenViewed(screen: AnalyticsScreen) {
        if (currentPreferences().analyticsEnabled && capabilities.analytics) {
            runCatching {
                backend.event(
                    "screen_view",
                    mapOf("screen_name" to screen.value, "screen_class" to screen.value),
                )
            }
        }
        setKey("current_screen", screen.value)
    }

    /** Identity is only retained locally; routes, words and slugs are never sent. */
    fun destinationChanged(
        identity: String,
        screen: AnalyticsScreen,
    ) {
        if (seenScreen.changed(identity)) screenViewed(screen)
    }

    override fun breadcrumb(message: String) {
        if (currentPreferences().crashReportingEnabled && capabilities.crashes) {
            runCatching { backend.breadcrumb("Diagnostic content omitted") }
        }
    }

    override fun setKey(
        key: String,
        value: String,
    ) {
        if (currentPreferences().crashReportingEnabled && capabilities.crashes) {
            safeCrashValue(key, value)?.let { safe -> runCatching { backend.key(key, safe) } }
        }
    }

    override fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String>,
    ) {
        if (throwable is kotlinx.coroutines.CancellationException ||
            (throwable is IllegalArgumentException && throwable !is kotlinx.serialization.SerializationException)
        ) {
            return
        }
        if (currentPreferences().crashReportingEnabled && capabilities.crashes) {
            val safeContext =
                context
                    .mapNotNull { (key, value) -> safeCrashValue(key, value)?.let { key to it } }
                    .toMap()
            runCatching {
                backend.nonFatal(
                    operation.value,
                    throwable::class.simpleName?.take(64) ?: "Throwable",
                    safeContext,
                )
            }
        }
    }

    override fun start(
        url: String,
        method: String,
    ): NetworkPerformanceTrace {
        if (!performanceActive()) return NoOpNetworkPerformanceTrace
        val safeMethod =
            method
                .uppercase()
                .takeIf { it in setOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS") }
                ?: "GET"
        val trace =
            runCatching {
                backend.startNetwork(sanitizePerformanceUrl(url, trustedOrigin), safeMethod)
            }.getOrDefault(NoOpNetworkPerformanceTrace)
        return GatedTrace(trace, ::performanceActive)
    }

    private fun performanceActive(): Boolean =
        capabilities.performance && performanceEligible && currentPreferences().performanceMonitoringEnabled
}

class ScreenViewDeduplicator {
    private var lastIdentity: String? = null

    fun changed(identity: String): Boolean {
        if (lastIdentity == identity) return false
        lastIdentity = identity
        return true
    }
}

@OptIn(ExperimentalAtomicApi::class)
private class GatedTrace(
    private val trace: NetworkPerformanceTrace,
    private val enabled: () -> Boolean,
) : NetworkPerformanceTrace {
    private val stopped = AtomicBoolean(false)

    override fun response(
        statusCode: Int,
        contentType: String?,
        payloadSizeBytes: Long?,
    ) {
        if (!stopped.load() && enabled()) {
            runCatching {
                // MIME type is bounded to standard tokens; response header parameters are private.
                val safeType =
                    contentType?.substringBefore(';')?.trim()?.takeIf {
                        it in
                            setOf(
                                "application/json",
                                "text/plain",
                                "text/html",
                                "application/octet-stream",
                                "image/png",
                                "image/jpeg",
                                "image/webp",
                                "image/svg+xml",
                            )
                    }
                trace.response(statusCode, safeType, payloadSizeBytes?.takeIf { it >= 0 })
            }
        }
    }

    override fun stop() {
        if (stopped.compareAndSet(false, true)) runCatching { trace.stop() }
    }
}

class PreferencesReportingCoordinator(
    private val preferences: Flow<ReportingPreferences>,
    private val controller: ReportingController,
) : ReportingCoordinator {
    override suspend fun initialize() {
        preferences.map { it }.distinctUntilChanged().collect { controller.synchronizeCollection() }
    }
}
