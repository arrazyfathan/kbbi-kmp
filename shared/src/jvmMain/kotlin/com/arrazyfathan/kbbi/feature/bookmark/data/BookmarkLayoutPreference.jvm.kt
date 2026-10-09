package com.arrazyfathan.kbbi.feature.bookmark.data

private const val BOOKMARK_LAYOUT_KEY = "bookmark_layout"

private val preferences by lazy {
    java.util.prefs.Preferences
        .userRoot()
        .node("com/arrazyfathan/kbbi/ui")
}

internal actual fun readBookmarkLayoutPreference(): String? = preferences.get(BOOKMARK_LAYOUT_KEY, null)

internal actual fun writeBookmarkLayoutPreference(value: String) {
    preferences.put(BOOKMARK_LAYOUT_KEY, value)
}
