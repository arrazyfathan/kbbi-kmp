package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import kbbi_kmp.shared.generated.resources.Res
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
)

sealed interface DetailAction {
    data class OnStarted(
        val word: String,
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
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null
    private var bookmarkUpdateJob: Job? = null
    private var translationJob: Job? = null

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> {
                observeSavedState(action.word)
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
            _state.update { it.copy(isTranslationEnabled = false, isTranslationLoading = false) }
            return
        }
        if (state.value.translation != null) {
            _state.update { it.copy(isTranslationEnabled = true) }
            return
        }
        if (translationJob?.isActive == true) return

        _state.update { it.copy(isTranslationLoading = true) }
        translationJob =
            viewModelScope.launch {
                when (val result = getWordTranslation(word)) {
                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Success -> {
                        _state.update {
                            it.copy(
                                isTranslationEnabled = true,
                                isTranslationLoading = false,
                                translation = result.data,
                            )
                        }
                    }

                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Error -> {
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
                            _state.update { it.copy(isSaved = false) }
                            _events.send(DetailEvent.ShowMessage(Res.string.word_deleted_success))
                        }
                    } else {
                        if (saveBookmark(word, wordList, visitorCount, aiGenerated)) {
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
