package com.arrazyfathan.kbbi.feature.settings.data

import androidx.core.content.edit
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext

private val preferences
    get() =
        appContext.getSharedPreferences(
            "kbbi_ui_preferences",
            android.content.Context.MODE_PRIVATE,
        )

internal actual fun readSettingsPreference(key: String): String? = preferences.getString(key, null)

internal actual fun writeSettingsPreference(
    key: String,
    value: String,
) {
    preferences.edit { putString(key, value) }
}

@Suppress("DEPRECATION")
internal actual fun applySettingsLanguage(languageTag: String) {
    val locale = java.util.Locale.forLanguageTag(languageTag)
    java.util.Locale.setDefault(locale)
    val configuration = appContext.resources.configuration
    configuration.setLocale(locale)
    appContext.resources.updateConfiguration(configuration, appContext.resources.displayMetrics)
}
