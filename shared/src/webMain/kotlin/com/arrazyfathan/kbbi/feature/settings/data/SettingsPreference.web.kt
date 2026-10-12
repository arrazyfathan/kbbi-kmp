package com.arrazyfathan.kbbi.feature.settings.data

import web.dom.document
import web.storage.localStorage

private fun prefKey(key: String) = "kbbi.ui.$key"

internal actual fun readSettingsPreference(key: String): String? = runCatching { localStorage.getItem(prefKey(key)) }.getOrNull()

internal actual fun writeSettingsPreference(
    key: String,
    value: String,
) {
    runCatching { localStorage.setItem(prefKey(key), value) }
}

internal actual fun applySettingsLanguage(languageTag: String) {
    runCatching { document.documentElement?.setAttribute("lang", languageTag) }
}
