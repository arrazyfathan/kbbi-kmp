package com.arrazyfathan.kbbi.core.data.remote.network

import com.arrazyfathan.kbbi.core.observability.CrashOperation
import com.arrazyfathan.kbbi.core.observability.CrashReporter
import com.arrazyfathan.kbbi.core.observability.NetworkPerformanceReporter
import com.arrazyfathan.kbbi.core.observability.NoOpCrashReporter
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceReporter
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceTrace
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import kotlin.coroutines.cancellation.CancellationException

class ObservedRequestsConfig {
    var performance: NetworkPerformanceReporter = NoOpNetworkPerformanceReporter
    var crashes: CrashReporter = NoOpCrashReporter
}

val ObservedRequests =
    createClientPlugin("ObservedRequests", ::ObservedRequestsConfig) {
        val performance = pluginConfig.performance
        val crashes = pluginConfig.crashes
        on(Send) { request ->
            val method = request.method.value
            val trace =
                runCatching { performance.start(request.url.build().toString(), method) }.getOrDefault(
                    NoOpNetworkPerformanceTrace,
                )
            try {
                val call = proceed(request)
                val response = call.response
                runCatching {
                    trace.response(
                        response.status.value,
                        response.headers[HttpHeaders.ContentType],
                        response.headers[HttpHeaders.ContentLength]?.toLongOrNull(),
                    )
                }
                call
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // Transport outages/timeouts and validation errors are ordinary typed outcomes.
                if (error !is IllegalArgumentException &&
                    error::class
                        .simpleName
                        .orEmpty()
                        .let { name ->
                            listOf(
                                "Timeout",
                                "Connect",
                                "Socket",
                                "Host",
                                "Unresolved",
                                "IOException",
                            ).none { it in name }
                        }
                ) {
                    crashes.recordNonFatal(error, CrashOperation.NetworkRequest)
                }
                throw error
            } finally {
                runCatching { trace.stop() }
            }
        }
    }
