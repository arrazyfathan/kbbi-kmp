package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import js.numbers.JsInt
import js.numbers.JsNumbers.toJsInt
import js.numbers.JsNumbers.toKotlinInt
import js.reflect.unsafeCast
import kotlin.js.ExperimentalWasmJsInterop
import web.events.EventHandler
import web.history.history
import web.window.window

@Composable
internal actual fun BindBrowserNavigation(navigationState: NavigationState) {
    val snapshot = navigationState.snapshot()
    val controller =
        remember(navigationState) {
            BrowserNavigationController(
                initialSnapshot = snapshot,
                restoreSnapshot = navigationState::restore,
            )
        }

    DisposableEffect(controller) {
        controller.start()
        onDispose(controller::stop)
    }

    LaunchedEffect(snapshot) {
        controller.onNavigationChanged(snapshot)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private class BrowserNavigationController(
    initialSnapshot: NavigationSnapshot,
    private val restoreSnapshot: (NavigationSnapshot) -> Unit,
) {
    private val snapshots = mutableMapOf(INITIAL_HISTORY_INDEX to initialSnapshot)
    private var currentIndex = INITIAL_HISTORY_INDEX
    private var nextIndex = INITIAL_HISTORY_INDEX + 1

    fun start() {
        history.replaceState(INITIAL_HISTORY_INDEX.toJsInt(), "")
        window.onpopstate =
            EventHandler { event ->
                val index = event.state?.unsafeCast<JsInt>()?.toKotlinInt() ?: return@EventHandler
                val snapshot = snapshots[index] ?: return@EventHandler

                currentIndex = index
                restoreSnapshot(snapshot)
            }
    }

    fun stop() {
        window.onpopstate = null
    }

    fun onNavigationChanged(snapshot: NavigationSnapshot) {
        if (snapshots[currentIndex] == snapshot) return

        val index = nextIndex++
        snapshots[index] = snapshot
        currentIndex = index
        history.pushState(index.toJsInt(), "")
    }

    private companion object {
        const val INITIAL_HISTORY_INDEX = 0
    }
}
