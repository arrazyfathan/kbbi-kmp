package com.arrazyfathan.kbbi.feature.bookmark.data

import androidx.core.content.edit
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext

private const val PREFERENCES_NAME = "kbbi_ui_preferences"
private const val BOOKMARK_LAYOUT_KEY = "bookmark_layout"

internal actual fun readBookmarkLayoutPreference(): String? =
    appContext
        .getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
        .getString(BOOKMARK_LAYOUT_KEY, null)

internal actual fun writeBookmarkLayoutPreference(value: String) {
    appContext.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE).edit {
        putString(BOOKMARK_LAYOUT_KEY, value)
    }
}
