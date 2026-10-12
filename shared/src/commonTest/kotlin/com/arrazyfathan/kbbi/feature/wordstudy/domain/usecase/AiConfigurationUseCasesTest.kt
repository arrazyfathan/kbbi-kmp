package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AiConfigurationUseCasesTest {
    @Test
    fun baseUrlRequiresHttpsAndRejectsCredentialsQueryAndCompletionPath() {
        assertEquals(
            "https://api.example.com/v1",
            normalizeAiBaseUrl(" https://api.example.com/v1/ "),
        )
        assertNull(normalizeAiBaseUrl("http://api.example.com/v1"))
        assertNull(normalizeAiBaseUrl("https://user:pass@api.example.com/v1"))
        assertNull(normalizeAiBaseUrl("https://api.example.com/v1?token=secret"))
        assertNull(normalizeAiBaseUrl("https://api.example.com/v1/chat/completions"))
    }

    @Test
    fun saveConfigurationRejectsInvalidUrlBeforeWriting() =
        runTest {
            val repository = FakeAiConfigurationRepository()
            val result =
                SaveAiConfigurationUseCase(repository)(
                    null,
                    "Provider",
                    "http://invalid.example",
                    listOf("model"),
                    "model",
                    "key",
                )
            assertEquals(AppResult.Error(DataError.BadRequest), result)
            assertEquals(0, repository.saveCount)
        }

    private class FakeAiConfigurationRepository : AiConfigurationRepository {
        override val configuration: Flow<AiConfigurationModel> = flowOf(AiConfigurationModel())
        var saveCount = 0

        override suspend fun saveCustomProvider(
            providerId: String?,
            name: String,
            baseUrl: String,
            models: List<String>,
            selectedModel: String,
            apiKey: String?,
        ): AppResult<String, DataError> {
            saveCount++
            return AppResult.Success("provider")
        }

        override suspend fun selectProviderMode(mode: AiProviderMode) = AppResult.Success(Unit)

        override suspend fun selectBackendProvider(
            providerId: String,
            model: String,
        ) = AppResult.Success(Unit)

        override suspend fun selectCustomProvider(
            providerId: String,
            model: String,
        ) = AppResult.Success(Unit)

        override suspend fun removeCustomProvider(providerId: String) = AppResult.Success(Unit)

        override suspend fun getCustomCredentials(providerId: String?): AppResult<AiCustomCredentialsModel, DataError> =
            AppResult.Error(DataError.BadRequest)
    }
}
