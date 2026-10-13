package com.arrazyfathan.kbbi.core.observability

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportingControllerTest {
    @Test fun defaultsAndImmediateOptOut() {
        var preferences = ReportingPreferences()
        val backend = RecordingBackend()
        val controller = ReportingController(backend, { preferences }, "https://api.example.test")
        controller.synchronizeCollection()
        assertEquals(listOf(true, false, false), backend.collectionState)
        controller.log(AnalyticsEvent.AppShared)
        assertTrue(backend.events.isEmpty())
        preferences = preferences.copy(analyticsEnabled = true)
        controller.log(AnalyticsEvent.AiWordStudyGenerated(AiProviderType.Custom, EventOutcome.Success))
        assertEquals("ai_word_study_generated", backend.events.single().first)
        assertEquals(mapOf("provider_mode" to "custom", "outcome" to "success"), backend.events.single().second)
        preferences = preferences.copy(analyticsEnabled = false, crashReportingEnabled = false)
        // No collector or restart is needed for reporter calls to obey the new values.
        controller.log(AnalyticsEvent.AppShared)
        controller.recordNonFatal(IllegalStateException("private-word key=secret"), CrashOperation.NetworkRequest)
        assertEquals(1, backend.events.size)
        assertTrue(backend.failures.isEmpty())
        controller.synchronizeCollection()
        assertEquals(listOf(false, false, false), backend.collectionState)
    }

    @Test fun performanceRequiresConsentProductionAndRelease() {
        for (flavor in listOf("production", "development")) {
            for (build in listOf("debug", "release")) {
                for (consent in listOf(false, true)) {
                    val backend = RecordingBackend(AppBuildInfo(flavor, build, "1.2.0"))
                    val controller =
                        ReportingController(
                            backend,
                            { ReportingPreferences(performanceMonitoringEnabled = consent) },
                            "https://api.example.test",
                        )
                    controller.synchronizeCollection()
                    controller.start("https://api.example.test/search/secret?visitor_id=secret", "GET").stop()
                    assertEquals(consent && flavor == "production" && build == "release", backend.traces.isNotEmpty())
                }
            }
        }
    }

    @Test fun unavailableCapabilitiesNeverReport() {
        val backend = RecordingBackend(capabilities = ReportingCapabilities())
        val controller = ReportingController(backend, { ReportingPreferences(true, true, true) }, "https://api.example.test")
        controller.log(AnalyticsEvent.AppShared)
        controller.screenViewed(AnalyticsScreen.Settings)
        controller.start("https://api.example.test/search/secret", "GET").stop()
        controller.recordNonFatal(IllegalStateException("secret"), CrashOperation.NetworkRequest)
        assertTrue(backend.events.isEmpty() && backend.traces.isEmpty() && backend.failures.isEmpty())
    }

    @Test fun crashesContainOnlyTypedContextAndExceptionType() {
        val backend = RecordingBackend()
        val controller = ReportingController(backend, { ReportingPreferences() }, "https://api.example.test")
        controller.setKey("current_screen", "secret-word")
        controller.setKey("api_key", "secret-key")
        controller.setKey("app_version", "1.2.0")
        controller.breadcrumb("private definitions, translations, visitor-id and secret-key")
        controller.recordNonFatal(
            IllegalStateException("secret-key", IllegalStateException("definition")),
            CrashOperation.ResponseDeserialization,
            mapOf("api_key" to "secret-key", "current_screen" to "word_detail", "operation" to "private-content"),
        )
        assertEquals(mapOf("app_version" to "1.2.0"), backend.keys)
        assertEquals("Diagnostic content omitted", backend.breadcrumbs.single())
        assertEquals(
            Triple("response_deserialization", "IllegalStateException", mapOf("current_screen" to "word_detail")),
            backend.failures.single(),
        )
        controller.recordNonFatal(CancellationException("secret"), CrashOperation.NetworkRequest)
        controller.recordNonFatal(IllegalArgumentException("secret"), CrashOperation.NetworkRequest)
        assertEquals(1, backend.failures.size)
        controller.recordNonFatal(
            kotlinx.serialization.SerializationException("secret response body"),
            CrashOperation.ResponseDeserialization,
        )
        assertEquals(2, backend.failures.size)
        assertEquals("SerializationException", backend.failures.last().second)
        assertFalse(backend.failures.toString().contains("secret"))
    }

    @Test fun screenIdentityDeduplicationSurvivesRepeatedObservers() {
        val backend = RecordingBackend()
        val controller = ReportingController(backend, { ReportingPreferences(analyticsEnabled = true) }, "https://api.example.test")
        controller.destinationChanged("word/bahasa", AnalyticsScreen.WordDetail)
        controller.destinationChanged("word/bahasa", AnalyticsScreen.WordDetail)
        controller.destinationChanged("settings", AnalyticsScreen.Settings)
        controller.destinationChanged("settings", AnalyticsScreen.Settings)
        controller.destinationChanged("word/bahasa", AnalyticsScreen.WordDetail)
        assertEquals(3, backend.events.size)
        assertTrue(backend.events.all { it.first == "screen_view" })
        assertFalse(backend.events.toString().contains("bahasa"))
    }

    @Test fun traceResponseSanitizesHeadersStopsOnceAndObeysOptOut() {
        var preferences = ReportingPreferences(performanceMonitoringEnabled = true)
        val backend = RecordingBackend()
        val controller = ReportingController(backend, { preferences }, "https://api.example.test")
        val trace = controller.start("https://api.example.test/search/secret?key=secret", "GET")
        assertEquals("https://api.example.test/search/_redacted_", backend.traces.single().first)
        trace.response(200, "application/json; private=secret", 4)
        assertEquals(
            Triple(200, "application/json", 4L),
            backend.traces
                .single()
                .second.responses
                .single(),
        )
        preferences = preferences.copy(performanceMonitoringEnabled = false)
        trace.response(201, "text/plain", 10)
        trace.stop()
        trace.stop()
        assertEquals(
            1,
            backend.traces
                .single()
                .second.responses.size,
        )
        assertEquals(
            1,
            backend.traces
                .single()
                .second.stops,
        )
    }

    @Test fun sourceEventNamesAndEnumParametersAreExact() {
        val search = AnalyticsEvent.SearchCompleted(EventSource.Suggestion, InputMethod.Text, EventOutcome.NotFound, suggestionUsed = true)
        assertEquals("search_completed", search.name)
        assertEquals(
            mapOf(
                "source" to "suggestion",
                "input_method" to "text",
                "outcome" to "not_found",
                "suggestion_used" to "true",
                "random_selection" to "false",
            ),
            search.parameters,
        )
        assertEquals("bookmark_changed", AnalyticsEvent.BookmarkChanged(BookmarkAction.Added, AnalyticsScreen.WordDetail).name)
        assertEquals("translation_changed", AnalyticsEvent.TranslationChanged(TranslationAction.Enabled, EventOutcome.Success, true).name)
        assertEquals("proverb_opened", AnalyticsEvent.ProverbOpened(EventOutcome.Success).name)
        assertEquals("content_opened", AnalyticsEvent.ContentOpened(ContentType.Word, EventSource.Bookmarks).name)
    }
}

internal class RecordingBackend(
    override val buildInfo: AppBuildInfo = AppBuildInfo("production", "release", "1.2.0"),
    override val capabilities: ReportingCapabilities = ReportingCapabilities(true, true, true),
) : ReportingBackend {
    var collectionState = emptyList<Boolean>()
    val events = mutableListOf<Pair<String, Map<String, String>>>()
    val keys = mutableMapOf<String, String>()
    val breadcrumbs = mutableListOf<String>()
    val failures = mutableListOf<Triple<String, String, Map<String, String>>>()
    val traces = mutableListOf<Pair<String, RecordingTrace>>()

    override fun collection(
        crashes: Boolean,
        analytics: Boolean,
        performance: Boolean,
    ) {
        collectionState =
            listOf(crashes, analytics, performance)
    }

    override fun event(
        name: String,
        parameters: Map<String, String>,
    ) {
        events.add(name to parameters)
    }

    override fun breadcrumb(message: String) {
        breadcrumbs.add(message)
    }

    override fun key(
        key: String,
        value: String,
    ) {
        keys[key] = value
    }

    override fun nonFatal(
        operation: String,
        originalType: String,
        context: Map<String, String>,
    ) {
        failures.add(Triple(operation, originalType, context))
    }

    override fun startNetwork(
        url: String,
        method: String,
    ): NetworkPerformanceTrace = RecordingTrace().also { traces.add(url to it) }
}

internal class RecordingTrace : NetworkPerformanceTrace {
    val responses = mutableListOf<Triple<Int, String?, Long?>>()
    var stops = 0

    override fun response(
        statusCode: Int,
        contentType: String?,
        payloadSizeBytes: Long?,
    ) {
        responses.add(Triple(statusCode, contentType, payloadSizeBytes))
    }

    override fun stop() {
        stops++
    }
}
