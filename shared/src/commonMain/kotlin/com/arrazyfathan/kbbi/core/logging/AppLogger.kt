package com.arrazyfathan.kbbi.core.logging

import com.arrazyfathan.kbbi.core.observability.CrashOperation
import com.arrazyfathan.kbbi.core.observability.CrashReporter
import com.arrazyfathan.kbbi.core.observability.NoOpCrashReporter
import com.arrazyfathan.kbbi.core.observability.sanitizedDiagnostic

object AppLogger {
    private var remote: CrashReporter = NoOpCrashReporter
    private var diagnosticsEnabled = false

    fun installRemoteSink(reporter: CrashReporter) {
        remote = reporter
    }

    fun configureDiagnostics(enabled: Boolean) {
        diagnosticsEnabled = enabled
    }

    // Raw Ktor request/response output stays in the local development console.
    fun network(message: String) {
        if (!diagnosticsEnabled) return
        println("[Debug] KBBI-API: $message")
    }

    private enum class LogLevel {
        Verbose,
        Debug,
        Info,
        Warning,
        Error,
        Critical,
    }

    fun plantDebugTree() {
        diagnosticsEnabled = true
    }

    fun verbose(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Verbose, tag, message, throwable)
    }

    fun debug(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Debug, tag, message, throwable)
    }

    fun info(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Info, tag, message, throwable)
    }

    fun warning(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Warning, tag, message, throwable)
    }

    fun warn(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        warning(tag, message, throwable)
    }

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Error, tag, message, throwable)
    }

    fun error(
        tag: String,
        throwable: Throwable,
        message: String,
    ) {
        error(tag = tag, message = message, throwable = throwable)
    }

    fun critical(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        write(LogLevel.Critical, tag, message, throwable)
    }

    fun throwable(
        tag: String,
        throwable: Throwable,
        message: String = throwable.message ?: throwable::class.simpleName ?: "Throwable",
    ) {
        error(tag = tag, message = message, throwable = throwable)
    }

    private fun write(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val safeMessage = sanitizedDiagnostic(tag, message)
        val safeTag =
            tag.takeIf {
                it in
                    setOf(
                        "KBBI-API",
                        "KBBI-Speech",
                        "WidgetRefresh",
                        "DailyReminder",
                        "AppIcon",
                        "WordCatalog",
                    )
            } ?: "KBBI"
        if (diagnosticsEnabled) {
            println(
                "[$level] $safeTag: $safeMessage" + (
                    throwable?.let { " (${it::class.simpleName})" }
                        ?: ""
                ),
            )
        }
        if (level >= LogLevel.Warning) remote.breadcrumb(safeMessage)
        if (level >= LogLevel.Error && throwable != null) {
            val operation =
                when {
                    tag == "KBBI-API" &&
                        message.contains(
                            "serialization",
                            ignoreCase = true,
                        )
                    -> CrashOperation.ResponseDeserialization

                    tag == "KBBI-API" -> CrashOperation.NetworkRequest

                    tag == "WordCatalog" -> CrashOperation.LocalDataRead

                    else -> CrashOperation.UnexpectedFailure
                }
            remote.recordNonFatal(throwable, operation)
        }
    }
}
