package com.arrazyfathan.kbbi.feature.settings.data

import platform.Foundation.NSUserDefaults

private fun prefKey(key: String) = "kbbi.ui.$key"

internal actual fun readSettingsPreference(key: String): String? = NSUserDefaults.standardUserDefaults.stringForKey(prefKey(key))

internal actual fun writeSettingsPreference(
    key: String,
    value: String,
) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = prefKey(key))
}

internal actual fun applySettingsLanguage(languageTag: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageTag), forKey = "AppleLanguages")
}
