package com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.data.remote.network.post
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.data.mapper.toBackendDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendAiProviderCatalogResponseDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendWordStudyResponseDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendWordStudyResultDto
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import io.ktor.client.HttpClient

class BackendWordStudyRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getProviders(): AppResult<AiBackendProviderCatalogModel, DataError> =
        when (
            val result =
                httpClient.get<BackendAiProviderCatalogResponseDto>("/api/v1/ai/providers")
        ) {
            is AppResult.Error -> {
                result
            }

            is AppResult.Success -> {
                when {
                    !result.data.success -> AppResult.Error(DataError.AiProviderFailure)
                    result.data.data == null -> AppResult.Error(DataError.EmptyBody)
                    else -> AppResult.Success(result.data.data.toDomain())
                }
            }
        }

    suspend fun generate(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> =
        when (
            val result =
                httpClient.post<_, BackendWordStudyResponseDto>(
                    "/api/v1/ai/word-study",
                    request.toBackendDto(),
                )
        ) {
            is AppResult.Error -> {
                result
            }

            is AppResult.Success -> {
                when {
                    !result.data.success -> AppResult.Error(DataError.AiProviderFailure)
                    result.data.data == null -> AppResult.Error(DataError.EmptyBody)
                    !result.data.data.hasValidContent(request.word) -> AppResult.Error(DataError.AiInvalidContent)
                    else -> AppResult.Success(result.data.data.toDomain())
                }
            }
        }
}

internal fun BackendWordStudyResultDto.hasValidContent(word: String): Boolean =
    explanation.isNotBlank() && examples.hasMinimumNonBlankItems(2) &&
        usageNotes.hasMinimumNonBlankItems(
            2,
        ) && provider.isNotBlank() && model.isNotBlank() && relatedWords.hasValidRelatedWords(word)

internal fun List<String>.hasMinimumNonBlankItems(minimum: Int): Boolean = size >= minimum && all(String::isNotBlank)

internal fun List<String>.hasValidRelatedWords(word: String): Boolean {
    if (!hasMinimumNonBlankItems(3)) return false
    val normalized = map { it.trim().lowercase() }
    return word.trim().lowercase() !in normalized && normalized.distinct().size == normalized.size
}
