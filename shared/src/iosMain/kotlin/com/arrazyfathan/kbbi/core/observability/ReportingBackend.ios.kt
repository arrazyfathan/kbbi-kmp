package com.arrazyfathan.kbbi.core.observability

/** The Swift application installs its native SDK adapter before starting Koin. */
object IosReporting {
    private var backend: ReportingBackend = NoOpReportingBackend()

    fun install(backend: ReportingBackend) {
        this.backend = backend
    }

    internal fun current(): ReportingBackend = backend
}

internal actual fun createReportingBackend(): ReportingBackend = IosReporting.current()
