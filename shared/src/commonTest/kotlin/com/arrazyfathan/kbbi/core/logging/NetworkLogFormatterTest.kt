package com.arrazyfathan.kbbi.core.logging

import kotlin.test.Test
import kotlin.test.assertContains

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
    fun preservesHeadersAndJsonValuesInCustomFormat() {
        val formatted =
            NetworkLogFormatter.format(
                """
                REQUEST: https://example.test
                Authorization: Bearer secret-token
                BODY START
                {"password":"secret-password","definition":"private-definition","translation":"private-translation","visitor_id":"private-visitor"}
                BODY END
                """.trimIndent(),
            )

        assertContains(formatted, "Authorization: Bearer secret-token")
        assertContains(formatted, "secret-password")
        assertContains(formatted, "private-definition")
        assertContains(formatted, "private-translation")
        assertContains(formatted, "private-visitor")
        assertContains(formatted, "NETWORK REQUEST")
        assertContains(formatted, "HEADERS")
        assertContains(formatted, "BODY")
        assertContains(formatted, "curl --location")
    }
}
