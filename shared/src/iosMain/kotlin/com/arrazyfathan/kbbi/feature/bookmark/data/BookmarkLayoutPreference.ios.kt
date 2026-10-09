package com.arrazyfathan.kbbi.feature.bookmark.data

import platform.Foundation.NSUserDefaults

private const val BOOKMARK_LAYOUT_KEY = "kbbi.ui.bookmark_layout"

internal actual fun readBookmarkLayoutPreference(): String? = NSUserDefaults.standardUserDefaults.stringForKey(BOOKMARK_LAYOUT_KEY)

internal actual fun writeBookmarkLayoutPreference(value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = BOOKMARK_LAYOUT_KEY)
}
