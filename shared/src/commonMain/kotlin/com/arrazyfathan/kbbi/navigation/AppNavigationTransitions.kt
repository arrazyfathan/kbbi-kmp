package com.arrazyfathan.kbbi.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val TOP_LEVEL_TRANSITION_DURATION_MILLIS = 250
private const val TOP_LEVEL_INCOMING_FADE_DURATION_MILLIS = 160
private const val TOP_LEVEL_INCOMING_FADE_DELAY_MILLIS = 90
private const val TOP_LEVEL_OUTGOING_FADE_DURATION_MILLIS = 90
private const val TOP_LEVEL_OFFSET_DIVISOR = 20
private const val HIERARCHICAL_TRANSITION_DURATION_MILLIS = 300
private const val HIERARCHICAL_INCOMING_FADE_DURATION_MILLIS = 210
private const val HIERARCHICAL_INCOMING_FADE_DELAY_MILLIS = 50
private const val HIERARCHICAL_OUTGOING_FADE_DURATION_MILLIS = 100
private const val HIERARCHICAL_INCOMING_OFFSET_DIVISOR = 10
private const val HIERARCHICAL_OUTGOING_OFFSET_DIVISOR = 20

internal enum class AppNavigationMotion {
    TopLevelForward,
    TopLevelBackward,
    HierarchicalPush,
    HierarchicalPop,
}

internal fun resolveAppNavigationMotion(
    initialContentKey: Any?,
    targetContentKey: Any?,
    isPop: Boolean,
): AppNavigationMotion {
    val initialDestination = initialContentKey.toAppNavigationDestination()
    val targetDestination = targetContentKey.toAppNavigationDestination()
    val initialTopLevelIndex = initialDestination
    val targetTopLevelIndex = targetDestination

    if (initialTopLevelIndex != null && targetTopLevelIndex != null) {
        return if (targetTopLevelIndex > initialTopLevelIndex) {
            AppNavigationMotion.TopLevelForward
        } else {
            AppNavigationMotion.TopLevelBackward
        }
    }

    return if (isPop) AppNavigationMotion.HierarchicalPop else AppNavigationMotion.HierarchicalPush
}

private fun Any?.toAppNavigationDestination(): Int? =
    when (this as? String) {
        "/home" -> 0
        "/words" -> 1
        "/bookmarks" -> 2
        else -> null
    }

internal fun appNavigationTransition(motion: AppNavigationMotion): ContentTransform =
    when (motion) {
        AppNavigationMotion.TopLevelForward -> topLevelNavigationTransition(direction = 1)
        AppNavigationMotion.TopLevelBackward -> topLevelNavigationTransition(direction = -1)
        AppNavigationMotion.HierarchicalPush -> hierarchicalNavigationTransition(direction = 1)
        AppNavigationMotion.HierarchicalPop -> hierarchicalNavigationTransition(direction = -1)
    }

private fun topLevelNavigationTransition(direction: Int): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = TOP_LEVEL_TRANSITION_DURATION_MILLIS),
            initialOffsetX = { fullWidth -> direction * fullWidth / TOP_LEVEL_OFFSET_DIVISOR },
        ) +
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = TOP_LEVEL_INCOMING_FADE_DURATION_MILLIS,
                        delayMillis = TOP_LEVEL_INCOMING_FADE_DELAY_MILLIS,
                    ),
            )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(durationMillis = TOP_LEVEL_TRANSITION_DURATION_MILLIS),
            targetOffsetX = { fullWidth -> -direction * fullWidth / TOP_LEVEL_OFFSET_DIVISOR },
        ) +
            fadeOut(
                animationSpec = tween(durationMillis = TOP_LEVEL_OUTGOING_FADE_DURATION_MILLIS),
            )
    )

private fun hierarchicalNavigationTransition(direction: Int): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = HIERARCHICAL_TRANSITION_DURATION_MILLIS),
            initialOffsetX = { fullWidth -> direction * fullWidth / HIERARCHICAL_INCOMING_OFFSET_DIVISOR },
        ) +
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = HIERARCHICAL_INCOMING_FADE_DURATION_MILLIS,
                        delayMillis = HIERARCHICAL_INCOMING_FADE_DELAY_MILLIS,
                    ),
            )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(durationMillis = HIERARCHICAL_TRANSITION_DURATION_MILLIS),
            targetOffsetX = { fullWidth -> -direction * fullWidth / HIERARCHICAL_OUTGOING_OFFSET_DIVISOR },
        ) +
            fadeOut(
                animationSpec = tween(durationMillis = HIERARCHICAL_OUTGOING_FADE_DURATION_MILLIS),
            )
    )
