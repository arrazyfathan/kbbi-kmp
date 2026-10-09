package com.arrazyfathan.kbbi.feature.bookmark.data

import com.arrazyfathan.kbbi.feature.bookmark.domain.BookmarkLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal expect fun readBookmarkLayoutPreference(): String?

internal expect fun writeBookmarkLayoutPreference(value: String)

class BookmarkLayoutPreferenceStore {
    private val mutableLayout =
        MutableStateFlow(BookmarkLayout.fromStorageKey(readBookmarkLayoutPreference()))
    val layout: StateFlow<BookmarkLayout> = mutableLayout

    fun setLayout(layout: BookmarkLayout) {
        writeBookmarkLayoutPreference(layout.storageKey)
        mutableLayout.value = layout
    }
}
