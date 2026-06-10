package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
internal fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: List<NavKey>,
): NavigationState {
    require(startRoute in topLevelRoutes) {
        "The start route must be one of the top-level routes."
    }

    val topLevelRouteSaver =
        remember(topLevelRoutes) {
            Saver<MutableState<NavKey>, Int>(
                save = { state -> topLevelRoutes.indexOf(state.value).takeIf { it >= 0 } },
                restore = { index -> mutableStateOf(topLevelRoutes.getOrElse(index) { startRoute }) },
            )
        }
    val topLevelRoute =
        rememberSaveable(startRoute, topLevelRoutes, saver = topLevelRouteSaver) {
            mutableStateOf(startRoute)
        }
    val backStacks =
        topLevelRoutes.associateWith { key ->
            rememberNavBackStack(navigationSavedStateConfiguration, key)
        }

    return remember(startRoute, topLevelRoutes, topLevelRoute, backStacks) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

internal class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute

    val currentRoute: NavKey
        get() = currentBackStack.last()

    private val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries =
            backStacks.mapValues { (_, stack) ->
                rememberDecoratedNavEntries(
                    backStack = stack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                        ),
                    entryProvider = entryProvider,
                )
            }

        return topLevelRoutesInUse().flatMap { decoratedEntries.getValue(it) }
    }

    private fun topLevelRoutesInUse(): List<NavKey> =
        if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}
