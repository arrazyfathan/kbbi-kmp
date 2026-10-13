package com.arrazyfathan.kbbi.core.observability

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import com.arrazyfathan.kbbi.getAppVersionName
import com.arrazyfathan.kbbi.shared.BuildKonfig
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import java.util.concurrent.atomic.AtomicBoolean

internal actual fun createReportingBackend(): ReportingBackend {
    val build =
        AppBuildInfo(
            BuildKonfig.APP_ENVIRONMENT,
            if (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) "debug" else "release",
            getAppVersionName(),
        )
    val app =
        FirebaseApp.getApps(appContext).firstOrNull() ?: runCatching {
            FirebaseApp.initializeApp(appContext)
        }.getOrNull()
    if (app == null) return NoOpReportingBackend(build)
    return runCatching { AndroidFirebaseReportingBackend(build) }.getOrElse {
        NoOpReportingBackend(
            build,
        )
    }
}

private class AndroidFirebaseReportingBackend(
    override val buildInfo: AppBuildInfo,
) : ReportingBackend {
    private val analytics = FirebaseAnalytics.getInstance(appContext)
    private val crashes = FirebaseCrashlytics.getInstance()
    private val performance = FirebasePerformance.getInstance()
    override val capabilities =
        ReportingCapabilities(analytics = true, crashes = true, performance = true)

    override fun collection(
        crashes: Boolean,
        analytics: Boolean,
        performance: Boolean,
    ) {
        this.analytics.setAnalyticsCollectionEnabled(analytics)
        this.crashes.isCrashlyticsCollectionEnabled = crashes
        this.performance.isPerformanceCollectionEnabled = performance
        if (!crashes) this.crashes.deleteUnsentReports()
    }

    override fun event(
        name: String,
        parameters: Map<String, String>,
    ) {
        analytics.logEvent(
            name,
            Bundle().apply { parameters.forEach { (k, v) -> putString(k, v) } },
        )
    }

    override fun breadcrumb(message: String) {
        crashes.log(message)
    }

    override fun key(
        key: String,
        value: String,
    ) {
        crashes.setCustomKey(key, value)
    }

    override fun nonFatal(
        operation: String,
        originalType: String,
        context: Map<String, String>,
    ) {
        crashes.setCustomKey("operation", operation)
        context.forEach { (k, v) -> crashes.setCustomKey(k, v) }
        crashes.recordException(IllegalStateException("$operation failed ($originalType)"))
    }

    override fun startNetwork(
        url: String,
        method: String,
    ): NetworkPerformanceTrace {
        val metric = performance.newHttpMetric(url, method)
        metric.start()
        return object : NetworkPerformanceTrace {
            private val stopped = AtomicBoolean(false)

            override fun response(
                statusCode: Int,
                contentType: String?,
                payloadSizeBytes: Long?,
            ) {
                if (stopped.get()) return
                metric.setHttpResponseCode(statusCode)
                contentType?.let(metric::setResponseContentType)
                payloadSizeBytes?.let(metric::setResponsePayloadSize)
            }

            override fun stop() {
                if (stopped.compareAndSet(false, true)) metric.stop()
            }
        }
    }
}
