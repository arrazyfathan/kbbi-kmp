package com.arrazyfathan.kbbi.feature.bookmark.data

import web.storage.localStorage

private const val BOOKMARK_LAYOUT_KEY = "kbbi.ui.bookmark_layout"

internal actual fun readBookmarkLayoutPreference(): String? = runCatching { localStorage.getItem(BOOKMARK_LAYOUT_KEY) }.getOrNull()

internal actual fun writeBookmarkLayoutPreference(value: String) {
    runCatching { localStorage.setItem(BOOKMARK_LAYOUT_KEY, value) }
}
