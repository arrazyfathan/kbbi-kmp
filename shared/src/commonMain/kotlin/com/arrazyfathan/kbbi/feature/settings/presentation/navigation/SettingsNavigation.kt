package com.arrazyfathan.kbbi.feature.settings.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.settings.presentation.SettingsRoute
import com.arrazyfathan.kbbi.feature.settings.presentation.legal.OpenSourceLicensesScreen
import com.arrazyfathan.kbbi.feature.settings.presentation.legal.PrivacyPolicyScreen
import com.arrazyfathan.kbbi.feature.settings.presentation.legal.TermsConditionsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsKey : NavKey

@Serializable
data class LegalDocumentKey(val document: String) : NavKey

fun EntryProviderScope<NavKey>.settingsEntry(
    onBack: () -> Unit,
    onOpenLegal: (String) -> Unit,
) {
    entry<SettingsKey> { SettingsRoute(onBack = onBack, onOpenLegal = onOpenLegal) }
    entry<LegalDocumentKey> { key ->
        when (key.document) {
            "privacy" -> PrivacyPolicyScreen(onNavigateBack = onBack)
            "terms" -> TermsConditionsScreen(onNavigateBack = onBack)
            "licenses" -> OpenSourceLicensesScreen(onNavigateBack = onBack)
            else -> error("Unknown legal document: ${key.document}")
        }
    }
}
