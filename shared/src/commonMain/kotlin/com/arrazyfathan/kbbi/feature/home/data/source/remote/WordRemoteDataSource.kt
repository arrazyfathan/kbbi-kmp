package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.ListWordDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.TranslateDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.WordResultDto
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel
import io.ktor.client.HttpClient
import io.ktor.http.encodeURLPathPart

class WordRemoteDataSource(
    private val httpClient: HttpClient,
    private val visitorIdProvider: VisitorIdProvider,
) {
    suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError> =
        when (
            val result =
                httpClient.get<ListWordDto>(
                    route = "/search/${word.trim().encodeURLPathPart()}",
                    headers = mapOf(VISITOR_ID_HEADER to visitorIdProvider.getVisitorId()),
                )
        ) {
            is AppResult.Success -> result.data.toWordResult()
            is AppResult.Error -> result
        }

    suspend fun translate(word: String): AppResult<TranslateModel, DataError> =
        when (
            val result =
                httpClient.get<TranslateDto>(
                    route = "/translate/${word.trim().encodeURLPathPart()}",
                    queryParameters = mapOf("to" to TRANSLATE_TARGET_LANGUAGE),
                )
        ) {
            is AppResult.Success -> result.data.toTranslateResult()
            is AppResult.Error -> result
        }

    private fun ListWordDto.toWordResult(): AppResult<WordResultModel, DataError> {
        val result = data
        return when {
            !success -> AppResult.Error(DataError.Remote(message))
            result == null -> AppResult.Error(DataError.EmptyBody)
            result.entries.isNotEmpty() -> AppResult.Success(result.toDomain())
            else -> AppResult.Error(DataError.NotFound)
        }
    }

    private fun TranslateDto.toTranslateResult(): AppResult<TranslateModel, DataError> =
        when {
            success && data != null -> AppResult.Success(data.toDomain())
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.EmptyBody)
        }

    private companion object {
        const val VISITOR_ID_HEADER = "x-visitor-id"
        const val TRANSLATE_TARGET_LANGUAGE = "en"
    }
}
