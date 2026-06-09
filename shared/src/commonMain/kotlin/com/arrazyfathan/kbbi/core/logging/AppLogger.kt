package com.arrazyfathan.kbbi.core.logging

object AppLogger {
    private enum class LogLevel {
        Verbose,
        Debug,
        Info,
        Warning,
        Error,
        Critical,
    }

    fun plantDebugTree() {
        // No-op in multiplatform
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
        val logMessage = "[$level] $tag: $message" + (throwable?.let { "\n${it.stackTraceToString()}" } ?: "")
        println(logMessage)
    }
}
