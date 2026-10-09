package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.feature.bookmark.data.BookmarkLayoutPreferenceStore
import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class BookmarksState(
    val bookmarks: List<ListWordModel> = emptyList(),
    val bookmarkLayout: BookmarkLayout = BookmarkLayout.GRID,
)

sealed interface BookmarksAction {
    data class OnLayoutSelected(
        val layout: BookmarkLayout,
    ) : BookmarksAction

    data class OnDeleteConfirmed(
        val word: String,
    ) : BookmarksAction
}

class BookmarksViewModel(
    observeBookmarks: ObserveBookmarksUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    private val bookmarkLayoutPreferenceStore: BookmarkLayoutPreferenceStore,
) : ViewModel() {
    val state: StateFlow<BookmarksState> =
        combine(observeBookmarks(), bookmarkLayoutPreferenceStore.layout) { bookmarks, layout ->
            BookmarksState(bookmarks = bookmarks, bookmarkLayout = layout)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookmarksState(),
        )

    fun onAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OnLayoutSelected -> {
                bookmarkLayoutPreferenceStore.setLayout(action.layout)
            }

            is BookmarksAction.OnDeleteConfirmed -> {
                viewModelScope.launch {
                    deleteBookmark(action.word)
                }
            }
        }
    }
}
