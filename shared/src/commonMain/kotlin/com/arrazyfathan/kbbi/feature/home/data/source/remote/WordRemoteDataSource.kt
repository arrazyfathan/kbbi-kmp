package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.ListWordDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.WordResultDto
import io.ktor.client.HttpClient

class WordRemoteDataSource(
    private val httpClient: HttpClient,
    private val visitorIdProvider: VisitorIdProvider,
) {
    suspend fun getMeaningOfWord(word: String): AppResult<WordResultDto, DataError> =
        when (
            val result =
                httpClient.get<ListWordDto>(
                    route = "/search/$word",
                    headers = mapOf(VISITOR_ID_HEADER to visitorIdProvider.getVisitorId()),
                )
        ) {
            is AppResult.Success -> result.data.toWordResult()
            is AppResult.Error -> result
        }

    private fun ListWordDto.toWordResult(): AppResult<WordResultDto, DataError> {
        val result = data
        return when {
            success && result != null && result.entries.isNotEmpty() -> AppResult.Success(result)
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.NotFound)
        }
    }

    private companion object {
        const val VISITOR_ID_HEADER = "x-visitor-id"
    }
}
