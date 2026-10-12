package com.arrazyfathan.kbbi.navigation

import androidx.navigation3.runtime.NavKey

internal class Navigator(
    private val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks) {
            state.topLevelRoute = route
        } else if (state.currentRoute != route && DestinationParser.isAvailable(route)) {
            val stack = state.backStacks.getValue(state.topLevelRoute)
            if (stack.size >= 32) stack.removeAt(1)
            stack.add(route)
        }
    }

    fun openExternal(route: NavKey) {
        if (!DestinationParser.isAvailable(route)) return
        if (state.currentRoute == route) return
        val root = if (route in state.backStacks) route else state.startRoute
        val stack = state.backStacks.getValue(root)
        stack.clear()
        stack.add(root)
        state.topLevelRoute = root
        if (route != root) stack.add(route)
    }

    fun goBack() {
        val currentStack = state.backStacks.getValue(state.topLevelRoute)

        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
