package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GenerateWordStudyUseCaseTest {
    @Test
    fun backendUsesServerDefaultAndNormalizesSource() =
        runTest {
            val repository = FakeStudyRepository()
            val result =
                GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)(
                    sampleSource(),
                    "ID",
                )

            assertIs<AppResult.Success<WordStudyModel>>(result)
            assertEquals(1, repository.backendCalls)
            assertEquals("bahasa", repository.lastRequest?.word)
            assertEquals("id", repository.lastRequest?.language)
            assertEquals(
                "sistem lambang bunyi bahasa",
                repository.lastRequest
                    ?.entries
                    ?.single()
                    ?.definitions
                    ?.single()
                    ?.description,
            )
        }

    @Test
    fun selectedBackendProviderAndModelAreForwarded() =
        runTest {
            val configuration =
                FakeConfigurationRepository(
                    AiConfigurationModel(backendProviderId = "gemini", backendModel = "flash"),
                )
            val repository = FakeStudyRepository()

            GenerateWordStudyUseCase(configuration, repository)(sampleSource(), "id")

            assertEquals("gemini", repository.lastRequest?.provider)
            assertEquals("flash", repository.lastRequest?.model)
        }

    @Test
    fun customModeReturnsCustomProviderErrorWithoutBackendFallback() =
        runTest {
            val configuration =
                FakeConfigurationRepository(
                    AiConfigurationModel(providerMode = AiProviderMode.CUSTOM),
                    customResult = AppResult.Error(DataError.AiProviderNotConfigured),
                )
            val repository = FakeStudyRepository()

            val result = GenerateWordStudyUseCase(configuration, repository)(sampleSource(), "en")

            assertEquals(AppResult.Error(DataError.AiProviderNotConfigured), result)
            assertEquals(0, repository.backendCalls)
            assertEquals(0, repository.customCalls)
        }

    @Test
    fun invalidLanguageOrEmptyAndOversizedSourceAreRejectedBeforeNetwork() =
        runTest {
            val repository = FakeStudyRepository()
            val useCase = GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)

            val invalidLanguage = useCase(sampleSource(), "fr")
            val emptyEntries = useCase(WordStudySourceModel("bahasa", emptyList()), "id")
            val tooManyEntries =
                useCase(
                    WordStudySourceModel(
                        "bahasa",
                        (1..11).map {
                            WordStudyEntryModel("word$it", listOf(WordStudyDefinitionModel("n", "meaning")))
                        },
                    ),
                    "id",
                )

            assertEquals(AppResult.Error(DataError.BadRequest), invalidLanguage)
            assertEquals(AppResult.Error(DataError.BadRequest), emptyEntries)
            assertEquals(AppResult.Error(DataError.BadRequest), tooManyEntries)
            assertEquals(0, repository.backendCalls + repository.customCalls)
        }

    private fun sampleSource() =
        WordStudySourceModel(
            word = " bahasa ",
            entries =
                listOf(
                    WordStudyEntryModel(
                        "bahasa",
                        listOf(WordStudyDefinitionModel(" n ", "sistem lambang bunyi --")),
                    ),
                ),
        )
}

private class FakeConfigurationRepository(
    initial: AiConfigurationModel = AiConfigurationModel(),
    private val customResult: AppResult<AiCustomCredentialsModel, DataError> =
        AppResult.Success(
            AiCustomCredentialsModel("custom", "Example", "https://example.com/v1", "model", "secret"),
        ),
) : AiConfigurationRepository {
    override val configuration: Flow<AiConfigurationModel> = MutableStateFlow(initial)

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ) = AppResult.Success(providerId ?: "custom")

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

    override suspend fun getCustomCredentials(providerId: String?) = customResult
}

private class FakeStudyRepository : WordStudyRepository {
    var backendCalls = 0
    var customCalls = 0
    var lastRequest: WordStudyRequestModel? = null

    override suspend fun getBackendProviders() = AppResult.Success(AiBackendProviderCatalogModel(null, emptyList()))

    override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> {
        backendCalls++
        lastRequest = request
        return AppResult.Success(sampleResult(AiProviderMode.BACKEND))
    }

    override suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError> {
        customCalls++
        lastRequest = request
        return AppResult.Success(sampleResult(AiProviderMode.CUSTOM))
    }
}

private fun sampleResult(mode: AiProviderMode) =
    WordStudyModel(
        explanation = "Simple",
        examples = listOf("One", "Two"),
        usageNotes = listOf("Use one", "Use two"),
        relatedWords = listOf("linguistik", "tutur", "bahasa daerah"),
        providerMode = mode,
        provider = "example",
        model = "model",
    )
