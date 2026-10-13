package com.arrazyfathan.kbbi.core.observability

import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.shared.BuildKonfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ObservedRequestsTest {
    @Test
    fun successfulAndHttpFailureRequestsCloseTraces() =
        runTest {
            for (status in listOf(HttpStatusCode.OK, HttpStatusCode.NotFound)) {
                val backend = RecordingBackend()
                val controller =
                    ReportingController(
                        backend,
                        { ReportingPreferences(performanceMonitoringEnabled = true) },
                        BuildKonfig.BASE_URL,
                    )
                val client =
                    HttpClientFactory(
                        Json,
                        MockEngine {
                            respond("{\"ok\":true}", status, headersOf("Content-Type", "application/json"))
                        },
                        controller,
                        controller,
                    ).build()
                try {
                    client.get<kotlinx.serialization.json.JsonObject>("/search/private-word")
                    assertEquals(1, backend.traces.size)
                    assertEquals(
                        1,
                        backend.traces
                            .single()
                            .second.stops,
                    )
                    assertEquals(
                        status.value,
                        backend.traces
                            .single()
                            .second.responses
                            .single()
                            .first,
                    )
                    assertTrue(backend.failures.isEmpty())
                } finally {
                    client.close()
                }
            }
        }

    @Test
    fun cancellationAndUnexpectedFailureCloseTraces() =
        runTest {
            for (cancelled in listOf(true, false)) {
                val backend = RecordingBackend()
                val controller =
                    ReportingController(
                        backend,
                        { ReportingPreferences(performanceMonitoringEnabled = true) },
                        BuildKonfig.BASE_URL,
                    )
                val client =
                    HttpClientFactory(
                        Json,
                        MockEngine {
                            if (cancelled) {
                                throw CancellationException("private")
                            } else {
                                throw IllegalStateException(
                                    "private",
                                )
                            }
                        },
                        controller,
                        controller,
                    ).build()
                try {
                    if (cancelled) {
                        var propagated = false
                        try {
                            client.get<kotlinx.serialization.json.JsonObject>("/search/private-word")
                        } catch (
                            _: CancellationException,
                        ) {
                            propagated = true
                        }
                        assertTrue(propagated)
                        assertTrue(backend.failures.isEmpty())
                    } else {
                        assertIs<AppResult.Error<*>>(client.get<kotlinx.serialization.json.JsonObject>("/search/private-word"))
                        assertEquals(1, backend.failures.size)
                    }
                    assertEquals(
                        1,
                        backend.traces
                            .single()
                            .second.stops,
                    )
                } finally {
                    client.close()
                }
            }
        }
}
