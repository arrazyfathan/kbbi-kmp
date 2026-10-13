package com.arrazyfathan.kbbi.core.observability

import io.ktor.http.Url

/** Query, fragment, user info, dynamic path segments and custom-provider origins are omitted. */
fun sanitizePerformanceUrl(
    url: String,
    trustedOrigin: String? = null,
): String {
    if (!Regex(
            "https?://.*",
            RegexOption.IGNORE_CASE,
        ).matches(url)
    ) {
        return "https://invalid.invalid/unknown"
    }
    val uri = runCatching { Url(url) }.getOrNull() ?: return "https://invalid.invalid/unknown"
    if (uri.protocol.name !in
        setOf(
            "http",
            "https",
        ) || uri.host.isBlank()
    ) {
        return "https://invalid.invalid/unknown"
    }
    val trusted = trustedOrigin?.let { runCatching { Url(it) }.getOrNull() }
    if (trustedOrigin != null &&
        (trusted == null || uri.host != trusted.host || uri.port != trusted.port || uri.protocol != trusted.protocol)
    ) {
        return "https://custom-provider.invalid/unknown"
    }
    val path = uri.encodedPath
    val safePath =
        when {
            path in
                setOf(
                    "/api/v1/figure",
                    "/api/v1/figure/search",
                    "/proverb",
                    "/proverb/search",
                    "/api/v1/ai/providers",
                    "/api/v1/ai/word-study",
                    "/top-words",
                )
            -> path

            path.startsWith("/api/v1/figure/") -> "/api/v1/figure/_redacted_"

            path.startsWith("/search/") -> "/search/_redacted_"

            path.startsWith("/translate/") -> "/translate/_redacted_"

            path.startsWith("/proverb/") -> "/proverb/_redacted_"

            else -> "/unknown"
        }
    val host = if (':' in uri.host) "[${uri.host}]" else uri.host
    val port = if (uri.port == uri.protocol.defaultPort) "" else ":${uri.port}"
    return "${uri.protocol.name}://$host$port$safePath"
}

internal fun safeCrashValue(
    key: String,
    value: String,
): String? =
    when (key) {
        "app_flavor" -> {
            value.takeIf { it in setOf("development", "production") }
        }

        "build_type" -> {
            value.takeIf { it in setOf("debug", "release") }
        }

        "app_version" -> {
            value.takeIf { Regex("[0-9]+\\.[0-9]+\\.[0-9]+").matches(it) }
        }

        "current_screen" -> {
            value.takeIf { v -> AnalyticsScreen.entries.any { it.value == v } }
        }

        "entry_point" -> {
            value.takeIf { v -> EventSource.entries.any { it.value == v } }
        }

        "operation" -> {
            value.takeIf { v -> CrashOperation.entries.any { it.value == v } }
        }

        "content_type" -> {
            value.takeIf { v -> ContentType.entries.any { it.value == v } }
        }

        "cache_hit" -> {
            value.takeIf { it == "true" || it == "false" }
        }

        "widget_type" -> {
            value.takeIf { v -> WidgetKind.entries.any { it.value == v } }
        }

        "reminder_type" -> {
            value.takeIf { v -> ReminderKind.entries.any { it.value == v } }
        }

        "download_state" -> {
            value.takeIf {
                it in
                    setOf(
                        "idle",
                        "started",
                        "completed",
                        "failed",
                        "cancelled",
                    )
            }
        }

        else -> {
            null
        }
    }

/** Free-form text never becomes a crash breadcrumb. */
fun sanitizedDiagnostic(
    tag: String,
    message: String,
): String =
    when {
        tag == "KBBI-API" && message in
            setOf(
                "Network request failed unexpectedly",
                "Network response serialization failed",
                "Response body serialization failed",
                "Response body processing failed unexpectedly",
            )
        -> message

        tag == "KBBI-API" -> "Network activity (content omitted)"

        tag == "KBBI-Speech" -> "Speech activity (content omitted)"

        else -> "Diagnostic content omitted"
    }
