package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.observability.AiProviderType
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.BookmarkAction
import com.arrazyfathan.kbbi.core.observability.EventOutcome
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.TranslationAction
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GenerateWordStudyUseCase
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.ai_word_study_error_generic
import kbbi_kmp.shared.generated.resources.ai_word_study_error_invalid_request
import kbbi_kmp.shared.generated.resources.ai_word_study_error_no_internet
import kbbi_kmp.shared.generated.resources.ai_word_study_error_rate_limited
import kbbi_kmp.shared.generated.resources.ai_word_study_error_timeout
import kbbi_kmp.shared.generated.resources.ai_word_study_error_unavailable
import kbbi_kmp.shared.generated.resources.translate_failed
import kbbi_kmp.shared.generated.resources.word_deleted_success
import kbbi_kmp.shared.generated.resources.word_saved_success
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@Immutable
data class DetailState(
    val isSaved: Boolean = false,
    val isBookmarkUpdating: Boolean = false,
    val isTranslationEnabled: Boolean = false,
    val isTranslationLoading: Boolean = false,
    val translation: TranslateModel? = null,
    val aiConfiguration: AiConfigurationModel = AiConfigurationModel(),
    val isWordStudyLoading: Boolean = false,
    val wordStudy: WordStudyModel? = null,
    val wordStudyErrorResId: StringResource? = null,
)

sealed interface DetailAction {
    data object OnGenerateWordStudy : DetailAction

    data object OnRetryWordStudy : DetailAction

    data object OnConfigureAi : DetailAction

    data class OnStarted(
        val word: ListWordModel,
        val language: String,
    ) : DetailAction

    data class OnBookmarkClick(
        val word: String,
        val wordList: List<WordModel>,
        val visitorCount: Int?,
        val aiGenerated: Boolean = false,
    ) : DetailAction

    data class OnTranslateToggled(
        val word: String,
        val enabled: Boolean,
    ) : DetailAction
}

sealed interface DetailEvent {
    data object NavigateToAiSettings : DetailEvent

    data class ShowMessage(
        val messageRes: StringResource,
        val isError: Boolean = false,
    ) : DetailEvent
}

class DetailViewModel(
    private val checkWordSaved: CheckWordSavedUseCase,
    private val saveBookmark: SaveBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    private val getWordTranslation: GetWordTranslationUseCase,
    private val generateWordStudy: GenerateWordStudyUseCase,
    private val aiConfigurationRepository: AiConfigurationRepository,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null
    private var bookmarkUpdateJob: Job? = null
    private var translationJob: Job? = null
    private var wordStudyJob: Job? = null
    private var currentWord: ListWordModel? = null
    private var currentLanguage = "id"
    private var currentStudyKey: StudyKey? = null

    init {
        viewModelScope.launch {
            aiConfigurationRepository.configuration.collect { configuration ->
                if (state.value.aiConfiguration != configuration) {
                    invalidateStudy()
                    _state.update { it.copy(aiConfiguration = configuration) }
                }
            }
        }
    }

    private fun invalidateStudy() {
        wordStudyJob?.cancel()
        currentStudyKey = null
        _state.update {
            it.copy(
                isWordStudyLoading = false,
                wordStudy = null,
                wordStudyErrorResId = null,
            )
        }
    }

    private fun generateStudy() {
        val word = currentWord ?: return
        val configuration = state.value.aiConfiguration
        if (configuration.providerMode == AiProviderMode.CUSTOM && !configuration.isCustomConfigurationComplete) {
            viewModelScope.launch { _events.send(DetailEvent.NavigateToAiSettings) }
            return
        }
        invalidateStudy()
        val key =
            StudyKey(word, currentLanguage, configuration.revision, configuration.providerMode)
        currentStudyKey = key
        _state.update { it.copy(isWordStudyLoading = true) }
        wordStudyJob =
            viewModelScope.launch {
                val result = generateWordStudy(word.toWordStudySourceModel(), key.language)
                if (currentStudyKey != key) return@launch
                when (result) {
                    is AppResult.Success -> {
                        analyticsReporter.log(
                            AnalyticsEvent.AiWordStudyGenerated(
                                if (configuration.providerMode == AiProviderMode.CUSTOM) {
                                    AiProviderType.Custom
                                } else {
                                    AiProviderType.Backend
                                },
                                EventOutcome.Success,
                            ),
                        )
                        _state.update { it.copy(isWordStudyLoading = false, wordStudy = result.data) }
                    }

                    is AppResult.Error -> {
                        analyticsReporter.log(
                            AnalyticsEvent.AiWordStudyGenerated(
                                if (configuration.providerMode == AiProviderMode.CUSTOM) {
                                    AiProviderType.Custom
                                } else {
                                    AiProviderType.Backend
                                },
                                EventOutcome.Error,
                            ),
                        )
                        _state.update {
                            it.copy(
                                isWordStudyLoading = false,
                                wordStudyErrorResId = result.error.toWordStudyErrorResource(),
                            )
                        }
                    }
                }
            }
    }

    fun onAction(action: DetailAction) {
        when (action) {
            DetailAction.OnGenerateWordStudy, DetailAction.OnRetryWordStudy -> {
                generateStudy()
            }

            DetailAction.OnConfigureAi -> {
                viewModelScope.launch { _events.send(DetailEvent.NavigateToAiSettings) }
            }

            is DetailAction.OnStarted -> {
                if (currentWord != action.word || currentLanguage != action.language) invalidateStudy()
                val wordChanged = currentWord?.word != action.word.word
                currentWord = action.word
                currentLanguage = action.language
                if (wordChanged) observeSavedState(action.word.word.lowercase())
            }

            is DetailAction.OnBookmarkClick -> {
                toggleBookmark(
                    action.word,
                    action.wordList,
                    action.visitorCount,
                    action.aiGenerated,
                )
            }

            is DetailAction.OnTranslateToggled -> {
                toggleTranslation(action.word, action.enabled)
            }
        }
    }

    private fun observeSavedState(word: String) {
        savedStateJob?.cancel()
        translationJob?.cancel()
        _state.update {
            it.copy(
                isTranslationEnabled = false,
                isTranslationLoading = false,
                translation = null,
            )
        }
        savedStateJob =
            viewModelScope.launch {
                checkWordSaved(word).collect { isSaved ->
                    _state.update { it.copy(isSaved = isSaved) }
                }
            }
    }

    private fun toggleTranslation(
        word: String,
        enabled: Boolean,
    ) {
        if (!enabled) {
            translationJob?.cancel()
            analyticsReporter.log(
                AnalyticsEvent.TranslationChanged(
                    TranslationAction.Disabled,
                    EventOutcome.Success,
                    state.value.translation != null,
                ),
            )
            _state.update { it.copy(isTranslationEnabled = false, isTranslationLoading = false) }
            return
        }
        if (state.value.translation != null) {
            analyticsReporter.log(
                AnalyticsEvent.TranslationChanged(
                    TranslationAction.Enabled,
                    EventOutcome.Success,
                    true,
                ),
            )
            _state.update { it.copy(isTranslationEnabled = true) }
            return
        }
        if (translationJob?.isActive == true) return

        _state.update { it.copy(isTranslationLoading = true) }
        translationJob =
            viewModelScope.launch {
                when (val result = getWordTranslation(word)) {
                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Success -> {
                        analyticsReporter.log(
                            AnalyticsEvent.TranslationChanged(
                                TranslationAction.Enabled,
                                EventOutcome.Success,
                                false,
                            ),
                        )
                        _state.update {
                            it.copy(
                                isTranslationEnabled = true,
                                isTranslationLoading = false,
                                translation = result.data,
                            )
                        }
                    }

                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Error -> {
                        analyticsReporter.log(
                            AnalyticsEvent.TranslationChanged(
                                TranslationAction.Enabled,
                                EventOutcome.Error,
                                false,
                            ),
                        )
                        _state.update { it.copy(isTranslationLoading = false) }
                        _events.send(
                            DetailEvent.ShowMessage(
                                Res.string.translate_failed,
                                isError = true,
                            ),
                        )
                    }
                }
            }
    }

    private fun toggleBookmark(
        word: String,
        wordList: List<WordModel>,
        visitorCount: Int?,
        aiGenerated: Boolean,
    ) {
        if (bookmarkUpdateJob?.isActive == true) return

        bookmarkUpdateJob =
            viewModelScope.launch {
                val wasSaved = state.value.isSaved
                _state.update { it.copy(isBookmarkUpdating = true) }
                try {
                    if (wasSaved) {
                        if (deleteBookmark(word)) {
                            analyticsReporter.log(
                                AnalyticsEvent.BookmarkChanged(
                                    BookmarkAction.Removed,
                                    AnalyticsScreen.WordDetail,
                                ),
                            )
                            _state.update { it.copy(isSaved = false) }
                            _events.send(DetailEvent.ShowMessage(Res.string.word_deleted_success))
                        }
                    } else {
                        if (saveBookmark(word, wordList, visitorCount, aiGenerated)) {
                            analyticsReporter.log(
                                AnalyticsEvent.BookmarkChanged(
                                    BookmarkAction.Added,
                                    AnalyticsScreen.WordDetail,
                                ),
                            )
                            _state.update { it.copy(isSaved = true) }
                            _events.send(DetailEvent.ShowMessage(Res.string.word_saved_success))
                        }
                    }
                } finally {
                    _state.update { it.copy(isBookmarkUpdating = false) }
                }
            }
    }
}

private data class StudyKey(
    val word: ListWordModel,
    val language: String,
    val revision: Long,
    val mode: AiProviderMode,
)

private fun DataError.toWordStudyErrorResource(): StringResource =
    when (this) {
        DataError.TooManyRequests -> Res.string.ai_word_study_error_rate_limited
        DataError.NoInternet -> Res.string.ai_word_study_error_no_internet
        DataError.RequestTimeout -> Res.string.ai_word_study_error_timeout
        DataError.BadRequest -> Res.string.ai_word_study_error_invalid_request
        DataError.ServiceUnavailable, DataError.ServerError, DataError.AiProviderFailure -> Res.string.ai_word_study_error_unavailable
        else -> Res.string.ai_word_study_error_generic
    }
