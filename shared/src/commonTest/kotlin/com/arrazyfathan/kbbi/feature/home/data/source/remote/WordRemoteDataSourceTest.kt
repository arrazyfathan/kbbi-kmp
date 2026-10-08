package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class WordRemoteDataSourceTest {
    @Test
    fun validWordResponseMapsEntriesAndVisitorMetadata() = runTest {
        val client = client { request ->
            assertEquals("/search/hello%20world", request.url.encodedPath)
            assertEquals("visitor-123", request.headers["x-visitor-id"])
            respond(
                content = """{"success":true,"message":"ok","data":{"word":"hello world","visitorCount":42,"aiGenerated":true,"entries":[{"headword":"hello","definitions":[{"wordClass":"interjection","description":"a greeting"}]}]}}""",
                headers = jsonHeaders,
            )
        }
        val result = WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord(" hello world ")
        val word = assertIs<AppResult.Success<com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel>>(result).data
        assertEquals("hello world", word.word)
        assertEquals(42, word.visitorCount)
        assertEquals(true, word.aiGenerated)
        assertEquals("a greeting", word.entries.single().meanings.single().description)
        client.close()
    }

    @Test
    fun emptyEnvelopeReturnsEmptyBody() = runTest {
        val client = client { respond("""{"success":true,"message":"ok"}""", headers = jsonHeaders) }
        assertEquals(AppResult.Error(DataError.EmptyBody), WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord("word"))
        client.close()
    }

    @Test
    fun emptyEntriesReturnNotFound() = runTest {
        val client = client { respond("""{"success":true,"message":"ok","data":{"word":"word","entries":[]}}""", headers = jsonHeaders) }
        assertEquals(AppResult.Error(DataError.NotFound), WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord("word"))
        client.close()
    }

    @Test
    fun malformedResponseReturnsSerializationError() = runTest {
        val client = client {
            respond(
                """{"success":true,"message":"ok","data":{"word":"word","entries":[{"headword":"word"}]}}""",
                headers = jsonHeaders,
            )
        }
        assertEquals(AppResult.Error(DataError.Serialization), WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord("word"))
        client.close()
    }

    @Test
    fun httpFailureMapsToServerError() = runTest {
        val client = client { respond("unavailable", status = HttpStatusCode.ServiceUnavailable) }
        assertEquals(AppResult.Error(DataError.ServiceUnavailable), WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord("word"))
        client.close()
    }

    @Test
    fun cancellationIsPropagated() = runTest {
        val client = client { throw CancellationException("cancelled") }
        assertFailsWith<CancellationException> {
            WordRemoteDataSource(client, FixedVisitorIdProvider).getMeaningOfWord("word")
        }
        client.close()
    }

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
        HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    private companion object {
        val jsonHeaders = io.ktor.http.headersOf(HttpHeaders.ContentType, "application/json")
        val FixedVisitorIdProvider = object : VisitorIdProvider {
            override fun getVisitorId() = "visitor-123"
        }
    }
}
