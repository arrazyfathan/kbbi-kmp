package com.arrazyfathan.kbbi.core.observability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReportingPrivacyTest {
    @Test
    fun removesDynamicArgumentsQueriesFragmentsAndUserInfo() {
        assertEquals(
            "https://api.example.test/search/_redacted_",
            sanitizePerformanceUrl("https://user:secret@api.example.test/search/rahasia?q=secret&visitor_id=secret#secret"),
        )
        assertEquals(
            "https://api.example.test/translate/_redacted_",
            sanitizePerformanceUrl("https://api.example.test/translate/%E7%A7%98%E5%AF%86?to=en"),
        )
        assertEquals(
            "https://api.example.test/proverb/_redacted_",
            sanitizePerformanceUrl("https://api.example.test/proverb/secret-title"),
        )
        assertEquals(
            "https://api.example.test/api/v1/figure/_redacted_",
            sanitizePerformanceUrl("https://api.example.test/api/v1/figure/secret"),
        )
    }

    @Test
    fun allowsOnlyStaticRoutesAndTrustedOrigins() {
        assertEquals(
            "https://api.example.test/proverb/search",
            sanitizePerformanceUrl("https://api.example.test/proverb/search?q=secret"),
        )
        assertEquals(
            "https://api.example.test/api/v1/figure",
            sanitizePerformanceUrl("https://api.example.test/api/v1/figure?page=1"),
        )
        assertEquals(
            "https://api.example.test/unknown",
            sanitizePerformanceUrl("https://api.example.test/private/secret"),
        )
        assertEquals(
            "https://custom-provider.invalid/unknown",
            sanitizePerformanceUrl(
                "https://secret-key.custom.test/chat/completions?secret=secret",
                "https://api.example.test",
            ),
        )
        assertEquals("https://invalid.invalid/unknown", sanitizePerformanceUrl("not a URL"))
    }

    @Test
    fun freeFormDiagnosticsNeverExposePayloads() {
        val secret = "definitions translations word visitor_id api_key=secret"
        assertFalse(sanitizedDiagnostic("KBBI-API", secret).contains("secret"))
        assertFalse(sanitizedDiagnostic(secret, secret).contains("secret"))
        assertEquals(
            "Response body serialization failed",
            sanitizedDiagnostic("KBBI-API", "Response body serialization failed"),
        )
    }
}
