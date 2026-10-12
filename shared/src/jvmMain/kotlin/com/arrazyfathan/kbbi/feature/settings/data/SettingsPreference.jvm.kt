package com.arrazyfathan.kbbi.feature.settings.data

private val preferences by lazy {
    java.util.prefs.Preferences
        .userRoot()
        .node("com/arrazyfathan/kbbi/ui")
}

internal actual fun readSettingsPreference(key: String): String? = preferences.get(key, null)

internal actual fun writeSettingsPreference(
    key: String,
    value: String,
) {
    preferences.put(key, value)
}

internal actual fun applySettingsLanguage(languageTag: String) {
    java.util.Locale.setDefault(java.util.Locale.forLanguageTag(languageTag))
}
