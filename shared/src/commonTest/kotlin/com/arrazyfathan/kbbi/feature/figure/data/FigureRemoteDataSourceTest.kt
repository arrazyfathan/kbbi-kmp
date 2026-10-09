package com.arrazyfathan.kbbi.feature.figure.data

import androidx.paging.PagingSource
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePagingException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class FigureRemoteDataSourceTest {
    @Test
    fun firstAndNextPagesUseSourcePagingContract() =
        runTest {
            var requests = 0
            val client =
                client { request ->
                    requests++
                    assertEquals("/api/v1/figure", request.url.encodedPath)
                    assertEquals("20", request.url.parameters["limit"])
                    val page = request.url.parameters["page"]
                    respond(
                        page(
                            page?.toInt() ?: -1,
                            hasNextPage = page == "1",
                            items = listOf(figureJson),
                        ),
                        headers = jsonHeaders,
                    )
                }
            val source = FigurePagingSource(FigureRemoteDataSource(client), "", false, 20)
            val first =
                assertIs<PagingSource.LoadResult.Page<Int, *>>(
                    source.load(
                        PagingSource.LoadParams.Refresh(
                            null,
                            20,
                            false,
                        ),
                    ),
                )
            val nextKey = first.nextKey ?: error("Expected another page")
            assertEquals(2, nextKey)
            val next =
                assertIs<PagingSource.LoadResult.Page<Int, *>>(
                    source.load(
                        PagingSource.LoadParams.Append(
                            nextKey,
                            20,
                            false,
                        ),
                    ),
                )
            assertEquals(1, next.prevKey)
            assertEquals(null, next.nextKey)
            assertEquals(2, requests)
            client.close()
        }

    @Test
    fun emptyPageIsAValidTerminalPage() =
        runTest {
            val client =
                client {
                    respond(
                        page(1, hasNextPage = false, items = emptyList()),
                        headers = jsonHeaders,
                    )
                }
            val result =
                FigurePagingSource(
                    FigureRemoteDataSource(client),
                    "",
                    false,
                    20,
                ).load(PagingSource.LoadParams.Refresh(null, 20, false))
            val loaded = assertIs<PagingSource.LoadResult.Page<Int, *>>(result)
            assertEquals(emptyList(), loaded.data)
            assertEquals(null, loaded.nextKey)
            client.close()
        }

    @Test
    fun queryUsesSearchEndpointAndPreservesParameters() =
        runTest {
            val client =
                client { request ->
                    assertEquals("/api/v1/figure/search", request.url.encodedPath)
                    assertEquals("Ki Hajar", request.url.parameters["q"])
                    assertEquals("3", request.url.parameters["page"])
                    assertEquals("true", request.url.parameters["includeDetails"])
                    respond(page(3, hasNextPage = false, items = emptyList()), headers = jsonHeaders)
                }
            val result = FigureRemoteDataSource(client).getFigures(3, 20, "Ki Hajar", true)
            assertIs<AppResult.Success<*>>(result)
            client.close()
        }

    @Test
    fun detailEncodesSlugAndMapsOptionalFields() =
        runTest {
            val client =
                client { request ->
                    assertEquals("/api/v1/figure/ki%20hajar", request.url.encodedPath)
                    respond(
                        """{"success":true,"message":"ok","data":{"name":"Ki Hajar","slug":"ki-hajar","sourceUrl":"https://kbbi.example/ki-hajar","photo":"javascript:alert(1)","description":7,"quotes":[" quote "," ",9]}}""",
                        headers = jsonHeaders,
                    )
                }
            val result =
                assertIs<AppResult.Success<com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel>>(
                    FigureRemoteDataSource(client).getFigure("ki hajar"),
                ).data
            assertEquals(null, result.photo)
            assertEquals(null, result.description)
            assertEquals(listOf("quote"), result.quotes)
            client.close()
        }

    @Test
    fun malformedPayloadAndHttpFailureAreMapped() =
        runTest {
            val malformedClient =
                client {
                    respond(
                        """{"success":true,"data":{"items":[{"slug":7}]}}""",
                        headers = jsonHeaders,
                    )
                }
            assertEquals(
                AppResult.Error(DataError.Serialization),
                FigureRemoteDataSource(malformedClient).getFigures(1, 20, "", false),
            )
            malformedClient.close()

            val failureClient =
                client { respond("unavailable", status = HttpStatusCode.ServiceUnavailable) }
            val pagingResult =
                FigurePagingSource(FigureRemoteDataSource(failureClient), "", false, 20).load(
                    PagingSource.LoadParams.Refresh(null, 20, false),
                )
            val pagingError =
                assertIs<PagingSource.LoadResult.Error<Int, com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel>>(
                    pagingResult,
                )
            assertEquals(
                DataError.ServiceUnavailable,
                (pagingError.throwable as FigurePagingException).dataError,
            )
            failureClient.close()
        }

    @Test
    fun cancellationIsPropagated() =
        runTest {
            val client = client { throw CancellationException("cancelled") }
            assertFailsWith<CancellationException> {
                FigureRemoteDataSource(client).getFigures(1, 20, "", false)
            }
            client.close()
        }

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData) =
        HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    private fun page(
        page: Int,
        hasNextPage: Boolean,
        items: List<String>,
    ): String =
        """{"success":true,"message":"ok","data":{"source":"test","pagination":{"page":$page,"limit":20,"total":${items.size},"totalPages":$page,"hasNextPage":$hasNextPage,"hasPreviousPage":${page > 1}},"items":[${
            items.joinToString(",")
        }]}}"""

    private companion object {
        const val figureJson =
            """{"name":"Ki Hajar","slug":"ki-hajar","sourceUrl":"https://kbbi.example/ki-hajar"}"""
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
