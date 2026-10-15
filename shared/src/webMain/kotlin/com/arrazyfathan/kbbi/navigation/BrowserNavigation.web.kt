package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.widgetcore.WidgetKind
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.context.GlobalContext
import web.events.EventHandler
import web.history.history
import web.window.window
import com.arrazyfathan.kbbi.core.observability.WidgetKind as AnalyticsWidgetKind

@Composable
internal actual fun BindBrowserNavigation(navigationState: NavigationState) {
    val controller = remember(navigationState) { BrowserNavigationController(navigationState) }
    DisposableEffect(controller) {
        controller.start()
        onDispose(controller::stop)
    }
    LaunchedEffect(controller) {
        snapshotFlow { navigationState.snapshot() }
            .distinctUntilChanged()
            .collect(controller::onNavigationChanged)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private class BrowserNavigationController(
    private val state: NavigationState,
) {
    private var currentSnapshot: NavigationSnapshot? = null
    private var lastWidgetLocation: String? = null
    private val previousHandler = window.onpopstate
    private val previousHashHandler = window.onhashchange

    fun start() {
        restoreFromLocation(history.state)
        currentSnapshot = state.snapshot()
        history.replaceState(NavigationCodec.encode(currentSnapshot!!).toJsString(), "", url())
        window.onpopstate = EventHandler { event -> restoreHistoryEntry(event.state) }
        window.onhashchange = EventHandler { restoreHistoryEntry(history.state) }
    }

    fun stop() {
        window.onpopstate = previousHandler
        window.onhashchange = previousHashHandler
    }

    private fun restoreHistoryEntry(historyState: JsAny?) {
        restoreFromLocation(historyState)
        currentSnapshot = state.snapshot()
        // Repair foreign, invalid, or unavailable entries without adding a new history item.
        history.replaceState(NavigationCodec.encode(currentSnapshot!!).toJsString(), "", url())
    }

    fun onNavigationChanged(snapshot: NavigationSnapshot) {
        if (snapshot == currentSnapshot) return
        currentSnapshot = snapshot
        history.pushState(NavigationCodec.encode(snapshot).toJsString(), "", url())
    }

    private fun restoreFromLocation(historyState: JsAny?) {
        val widgetLink = DestinationParser.widgetLink(window.location.hash)
        if (widgetLink != null) {
            val location = window.location.href
            if (location != lastWidgetLocation) {
                lastWidgetLocation = location
                val (kind, word) = widgetLink
                val action =
                    when (kind) {
                        WidgetKind.QUICK_SEARCH -> IncomingLaunchAction.FOCUS_SEARCH
                        WidgetKind.WORD_OF_DAY -> if (word == null) IncomingLaunchAction.FOCUS_SEARCH else IncomingLaunchAction.SEARCH
                        WidgetKind.SAVED_WORD -> if (word == null) IncomingLaunchAction.OPEN_BOOKMARKS else IncomingLaunchAction.SEARCH
                    }
                val destination =
                    if (action == IncomingLaunchAction.OPEN_BOOKMARKS) BookmarksKey else HomeKey
                Navigator(state).openExternal(destination)
                val analyticsKind =
                    when (kind) {
                        WidgetKind.QUICK_SEARCH -> AnalyticsWidgetKind.QuickSearch
                        WidgetKind.WORD_OF_DAY -> AnalyticsWidgetKind.WordOfDay
                        WidgetKind.SAVED_WORD -> AnalyticsWidgetKind.SavedWord
                    }
                GlobalContext
                    .get()
                    .get<AnalyticsReporter>()
                    .log(AnalyticsEvent.WidgetOpened(analyticsKind))
                if (action != IncomingLaunchAction.OPEN_BOOKMARKS) {
                    IncomingActionSignal.publish(
                        IncomingActionEvent(
                            "browser-widget-${kotlin.random.Random.nextLong()}",
                            action,
                            word,
                        ),
                    )
                }
            }
            return
        }
        lastWidgetLocation = null
        val encoded = historyStateString(historyState)?.toString()
        val snapshot = encoded?.let(NavigationCodec::decode)
        val destination = DestinationParser.parse(window.location.hash)
        if (snapshot != null && snapshot.isValid() && destination ==
            snapshot.backStacks
                .getValue(
                    snapshot.topLevelRoute,
                ).last()
        ) {
            state.restore(snapshot)
        } else {
            Navigator(state).openExternal(
                destination?.takeIf(DestinationParser::isAvailable)
                    ?: com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey,
            )
        }
    }

    private fun url(): String = "${window.location.pathname}${window.location.search}#${DestinationParser.path(state.currentRoute)}"
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun historyStateString(value: JsAny?): JsString? = js("typeof value === 'string' ? value : null")
