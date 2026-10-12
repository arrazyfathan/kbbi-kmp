package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GenerateWordStudyUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai.FakeAiSettingsConfigurationRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DetailWordStudyViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val word =
        ListWordModel(
            "bahasa",
            listOf(WordModel("bahasa", listOf(MeaningModel("n", "sistem lambang bunyi")))),
        )

    @BeforeTest
    fun setup() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun cleanup() = Dispatchers.resetMain()

    @Test
    fun providerRevisionClearsGeneratedStudy() =
        runTest(dispatcher) {
            val configuration = FakeAiSettingsConfigurationRepository()
            val repository = StudyRepository()
            val viewModel = create(configuration, repository)
            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertNotNull(viewModel.state.value.wordStudy)
            configuration.state.value =
                configuration.state.value.copy(backendProviderId = "another", revision = 1)
            assertNull(viewModel.state.value.wordStudy)
            assertFalse(viewModel.state.value.isWordStudyLoading)
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertEquals("another", repository.requests.last().provider)
        }

    @Test
    fun changingLanguageClearsResultAndUsesNewLanguage() =
        runTest(dispatcher) {
            val repository = StudyRepository()
            val viewModel = create(FakeAiSettingsConfigurationRepository(), repository)
            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            viewModel.onAction(DetailAction.OnStarted(word, "en"))
            assertNull(viewModel.state.value.wordStudy)
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertEquals("en", repository.requests.last().language)
        }

    @Test
    fun retryReplacesProviderErrorWithResult() =
        runTest(dispatcher) {
            val repository =
                StudyRepository().apply { outcome = AppResult.Error(DataError.RequestTimeout) }
            val viewModel = create(FakeAiSettingsConfigurationRepository(), repository)
            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertNotNull(viewModel.state.value.wordStudyErrorResId)
            repository.outcome = AppResult.Success(result())
            viewModel.onAction(DetailAction.OnRetryWordStudy)
            assertNull(viewModel.state.value.wordStudyErrorResId)
            assertNotNull(viewModel.state.value.wordStudy)
        }

    @Test
    fun incompleteCustomModeNavigatesToSettingsWithoutRequest() =
        runTest(dispatcher) {
            val configuration =
                FakeAiSettingsConfigurationRepository().apply {
                    state.value = AiConfigurationModel(providerMode = AiProviderMode.CUSTOM)
                }
            val repository = StudyRepository()
            val viewModel = create(configuration, repository)
            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertEquals(DetailEvent.NavigateToAiSettings, viewModel.events.first())
            assertTrue(repository.requests.isEmpty())
        }

    @Test
    fun providerChangeCancelsPendingStudyAndRejectsStaleCompletion() =
        runTest(dispatcher) {
            val configuration = FakeAiSettingsConfigurationRepository()
            val pending = CompletableDeferred<AppResult<WordStudyModel, DataError>>()
            val repository = StudyRepository().apply { waitFor = pending }
            val viewModel = create(configuration, repository)
            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertTrue(viewModel.state.value.isWordStudyLoading)
            configuration.state.value = configuration.state.value.copy(revision = 2)
            pending.complete(AppResult.Success(result()))
            assertFalse(viewModel.state.value.isWordStudyLoading)
            assertNull(viewModel.state.value.wordStudy)
        }

    @Test
    fun sourceMapperPreservesAiAttributionAndDefinitions() {
        val mapped = word.copy(aiGenerated = true).toWordStudySourceModel()
        assertTrue(mapped.aiGenerated)
        assertEquals(
            "sistem lambang bunyi",
            mapped.entries
                .single()
                .definitions
                .single()
                .description,
        )
    }

    private fun create(
        configuration: FakeAiSettingsConfigurationRepository,
        repository: StudyRepository,
    ): DetailViewModel {
        val bookmarks =
            object : BookmarkRepository {
                override suspend fun bookmarkWord(
                    word: String,
                    result: List<WordModel>,
                    visitorCount: Int?,
                    aiGenerated: Boolean,
                ) = true

                override suspend fun deleteWord(word: String) = true

                override fun checkIfWordIsSaved(word: String) = flowOf(false)

                override fun getBookmarks() = flowOf(emptyList<ListWordModel>())
            }
        val translations =
            object : TranslateRepository {
                override suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError> = AppResult.Error(DataError.Unknown)
            }
        return DetailViewModel(
            CheckWordSavedUseCase(bookmarks),
            SaveBookmarkUseCase(bookmarks),
            DeleteBookmarkUseCase(bookmarks),
            GetWordTranslationUseCase(translations),
            GenerateWordStudyUseCase(configuration, repository),
            configuration,
        )
    }

    private class StudyRepository : WordStudyRepository {
        val requests = mutableListOf<WordStudyRequestModel>()
        var outcome: AppResult<WordStudyModel, DataError> = AppResult.Success(result())
        var waitFor: CompletableDeferred<AppResult<WordStudyModel, DataError>>? = null

        override suspend fun getBackendProviders() = AppResult.Success(AiBackendProviderCatalogModel(null, emptyList()))

        override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> {
            requests += request
            return waitFor?.await() ?: outcome
        }

        override suspend fun generateWithCustomProvider(
            request: WordStudyRequestModel,
            credentials: AiCustomCredentialsModel,
        ) = generateWithBackend(request)
    }

    companion object {
        private fun result() =
            WordStudyModel(
                "Explanation",
                listOf("Example one", "Example two"),
                listOf("Usage one", "Usage two"),
                listOf("kata", "tutur", "ucap"),
                AiProviderMode.BACKEND,
                "provider",
                "model",
            )
    }
}
