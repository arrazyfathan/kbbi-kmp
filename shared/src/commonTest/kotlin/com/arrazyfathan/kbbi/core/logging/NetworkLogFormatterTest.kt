package com.arrazyfathan.kbbi.core.logging

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class NetworkLogFormatterTest {
    @Test
    fun formatsContentTypeCaseInsensitively() {
        val formatted =
            NetworkLogFormatter.format(
                """
                RESPONSE: 200 OK
                Content-Type: application/json
                """.trimIndent(),
            )

        assertContains(formatted, "Type   : application/json")
    }

    @Test
    fun redactsSensitiveHeadersAndJsonValues() {
        val formatted =
            NetworkLogFormatter.format(
                """
                REQUEST: https://example.test
                Authorization: Bearer secret-token
                BODY START
                {"password":"secret-password"}
                BODY END
                """.trimIndent(),
            )

        assertContains(formatted, "Authorization: <redacted>")
        assertContains(formatted, "\"password\": \"<redacted>\"")
        assertFalse(formatted.contains("secret-token"))
        assertFalse(formatted.contains("secret-password"))
    }
}
