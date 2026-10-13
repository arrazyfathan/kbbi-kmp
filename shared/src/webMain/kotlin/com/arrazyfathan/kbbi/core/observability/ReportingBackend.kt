package com.arrazyfathan.kbbi.core.observability

internal actual fun createReportingBackend(): ReportingBackend = NoOpReportingBackend()
