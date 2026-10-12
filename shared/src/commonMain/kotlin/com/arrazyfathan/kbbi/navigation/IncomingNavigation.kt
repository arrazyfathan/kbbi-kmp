package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import org.koin.compose.koinInject

/** Kept outside locale-dependent UI so consumed requests survive language changes. */
@Composable
internal fun BindIncomingNavigation(state: NavigationState) {
    val requests: IncomingLaunchRequests = koinInject()
    var consumedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    LaunchedEffect(state, requests) {
        val navigator = Navigator(state)
        requests.observe().collect { request ->
            if (request.requestId.isBlank() || request.requestId.length > 128 || request.requestId in consumedIds) {
                requests.acknowledge(request.requestId)
                return@collect
            }
            consumedIds = (consumedIds + request.requestId).takeLast(128)
            val destination =
                when {
                    request.uri != null -> DestinationParser.parse(request.uri)
                    request.sharedText != null -> DestinationParser.sharedText(request.sharedText)
                    else -> null
                }
            destination?.let(navigator::openExternal)
            requests.acknowledge(request.requestId)
        }
    }
}
