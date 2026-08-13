package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import kbbi_kmp.shared.generated.resources.Res
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

data class DetailState(
    val isSaved: Boolean = false,
    val isBookmarkUpdating: Boolean = false,
)

sealed interface DetailAction {
    data class OnStarted(
        val word: String,
    ) : DetailAction

    data class OnBookmarkClick(
        val word: String,
        val wordList: List<WordModel>,
        val visitorCount: Int?,
    ) : DetailAction
}

sealed interface DetailEvent {
    data class ShowMessage(
        val messageRes: StringResource,
    ) : DetailEvent
}

class DetailViewModel(
    private val checkWordSaved: CheckWordSavedUseCase,
    private val saveBookmark: SaveBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null
    private var bookmarkUpdateJob: Job? = null

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> observeSavedState(action.word)
            is DetailAction.OnBookmarkClick -> toggleBookmark(action.word, action.wordList, action.visitorCount)
        }
    }

    private fun observeSavedState(word: String) {
        savedStateJob?.cancel()
        savedStateJob =
            viewModelScope.launch {
                checkWordSaved(word).collect { isSaved ->
                    _state.update { it.copy(isSaved = isSaved) }
                }
            }
    }

    private fun toggleBookmark(
        word: String,
        wordList: List<WordModel>,
        visitorCount: Int?,
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
                        if (saveBookmark(word, wordList, visitorCount)) {
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
