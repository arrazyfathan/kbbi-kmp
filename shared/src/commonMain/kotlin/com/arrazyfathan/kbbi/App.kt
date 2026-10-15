package com.arrazyfathan.kbbi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.observability.ReportingCoordinator
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbKey
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderDestination
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.reminders.presentation.ReminderFallbackCards
import com.arrazyfathan.kbbi.feature.settings.data.SettingsPreferenceStore
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.navigation.BindBrowserNavigation
import com.arrazyfathan.kbbi.navigation.BindIncomingNavigation
import com.arrazyfathan.kbbi.navigation.BindReportingNavigation
import com.arrazyfathan.kbbi.navigation.MainApp
import com.arrazyfathan.kbbi.navigation.Navigator
import com.arrazyfathan.kbbi.navigation.rememberNavigationState
import com.arrazyfathan.kbbi.navigation.topLevelRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun App() {
    val reporting: ReportingCoordinator = koinInject()
    LaunchedEffect(reporting) { reporting.initialize() }
    val settings: SettingsPreferenceStore = koinInject()
    val reminderScheduler: ReminderScheduler = koinInject()
    val pendingReminders by reminderScheduler.pendingFallbacks.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(reminderScheduler) {
        reminderScheduler.reconcile()
        while (true) {
            reminderScheduler.tick()
            delay(60_000.milliseconds)
        }
    }
    val preferences by settings.preferences.collectAsStateWithLifecycle()
    var isSplashVisible by rememberSaveable { mutableStateOf(true) }
    val navigationState = rememberNavigationState(HomeKey, topLevelRoutes)
    BindIncomingNavigation(navigationState) { isSplashVisible = false }
    BindBrowserNavigation(navigationState)
    BindReportingNavigation(navigationState, !isSplashVisible)
    // Platform language is applied before preferences emit. Recreate resource
    // lookups for the new locale while restoring the current navigation state.
    key(preferences.language) {
        KBBITheme(theme = preferences.theme) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            ) {
                Box(
                    modifier = Modifier.align(Alignment.Center).widthIn(max = 840.dp).fillMaxSize(),
                ) {
                    if (isSplashVisible) {
                        SplashRoute(
                            onTimeout = {
                                isSplashVisible = false
                            },
                        )
                    } else {
                        MainApp(navigationState)
                    }
                }
                if (!isSplashVisible && pendingReminders.isNotEmpty()) {
                    ReminderFallbackCards(
                        reminders = pendingReminders,
                        onOpen = { reminder ->
                            val destination =
                                when (val target = reminder.destination) {
                                    is ReminderDestination.Word -> DetailKey(target.word)
                                    is ReminderDestination.Proverb -> ProverbKey(target.slug)
                                    ReminderDestination.Bookmarks -> BookmarksKey
                                }
                            Navigator(navigationState).openExternal(destination)
                            coroutineScope.launch {
                                reminderScheduler.dismiss(
                                    reminder.type,
                                    reminder.occurrenceDate,
                                )
                            }
                        },
                        onDismiss = { reminder ->
                            coroutineScope.launch {
                                reminderScheduler.dismiss(
                                    reminder.type,
                                    reminder.occurrenceDate,
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .widthIn(max = 840.dp),
                    )
                }
            }
        }
    }
}
