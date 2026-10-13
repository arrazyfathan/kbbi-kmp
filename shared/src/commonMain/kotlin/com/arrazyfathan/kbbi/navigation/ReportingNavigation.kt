package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.ReportingController
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureDetailKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.LegalDocumentKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.SettingsKey
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsKey
import org.koin.compose.koinInject

internal fun NavKey.analyticsScreen(): AnalyticsScreen? =
    when (this) {
        HomeKey -> {
            AnalyticsScreen.Home
        }

        WordsKey -> {
            AnalyticsScreen.Words
        }

        BookmarksKey -> {
            AnalyticsScreen.Bookmarks
        }

        is DetailKey -> {
            AnalyticsScreen.WordDetail
        }

        is ProverbKey -> {
            AnalyticsScreen.Proverbs
        }

        FigureKey -> {
            AnalyticsScreen.Figures
        }

        is FigureDetailKey -> {
            AnalyticsScreen.FigureDetail
        }

        SettingsKey -> {
            AnalyticsScreen.Settings
        }

        AiSettingsKey -> {
            AnalyticsScreen.AiSettings
        }

        is LegalDocumentKey -> {
            when (document) {
                "privacy" -> AnalyticsScreen.PrivacyPolicy
                "terms" -> AnalyticsScreen.TermsConditions
                "licenses" -> AnalyticsScreen.OpenSourceLicenses
                else -> null
            }
        }

        else -> {
            null
        }
    }

@Composable
internal fun BindReportingNavigation(
    state: NavigationState,
    visible: Boolean,
) {
    val reporter: ReportingController = koinInject()
    val route = state.currentRoute
    LaunchedEffect(route, visible, reporter) {
        if (visible) {
            route
                .analyticsScreen()
                ?.let { reporter.destinationChanged(DestinationParser.path(route), it) }
        }
    }
}
