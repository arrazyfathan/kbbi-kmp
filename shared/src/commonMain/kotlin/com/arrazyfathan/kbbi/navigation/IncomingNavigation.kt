package com.arrazyfathan.kbbi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject

internal data class IncomingActionEvent(
    val requestId: String,
    val action: IncomingLaunchAction,
    val query: String? = null,
)

internal object IncomingActionSignal {
    private val mutablePending = MutableStateFlow<List<IncomingActionEvent>>(emptyList())
    val pending = mutablePending.asStateFlow()

    fun publish(event: IncomingActionEvent) {
        mutablePending.update { events ->
            if (events.size >= 32 || events.any { it.requestId == event.requestId }) events else events + event
        }
    }

    fun consume(requestId: String) {
        mutablePending.update { events -> events.filterNot { it.requestId == requestId } }
    }
}

/** Kept outside locale-dependent UI so consumed requests survive language changes. */
@Composable
internal fun BindIncomingNavigation(
    state: NavigationState,
    onAccepted: () -> Unit = {},
) {
    val requests: IncomingLaunchRequests = koinInject()
    var consumedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    LaunchedEffect(state, requests, onAccepted) {
        val navigator = Navigator(state)
        requests.observe().collect { request ->
            if (request.requestId.isBlank() || request.requestId.length > 128 || request.requestId in consumedIds) {
                requests.acknowledge(request.requestId)
                return@collect
            }
            consumedIds = (consumedIds + request.requestId).takeLast(128)
            val sharedWord =
                (request.sharedText?.let(DestinationParser::sharedText) as? DetailKey)?.word
            val action =
                request.action
                    ?: if (sharedWord != null) {
                        IncomingLaunchAction.SEARCH
                    } else {
                        when (request.uri) {
                            "kbbi://action/search" -> IncomingLaunchAction.FOCUS_SEARCH
                            "kbbi://action/random" -> IncomingLaunchAction.RANDOM_WORD
                            else -> null
                        }
                    }
            val destination =
                when {
                    action != null -> {
                        when (action) {
                            IncomingLaunchAction.SEARCH,
                            IncomingLaunchAction.FOCUS_SEARCH,
                            IncomingLaunchAction.RANDOM_WORD,
                            IncomingLaunchAction.OPEN_HOME,
                            -> HomeKey

                            IncomingLaunchAction.OPEN_BOOKMARKS -> BookmarksKey

                            IncomingLaunchAction.OPEN_PROVERBS -> ProverbKey(null)
                        }
                    }

                    request.uri != null -> {
                        DestinationParser.parse(request.uri)
                    }

                    else -> {
                        null
                    }
                }
            destination?.takeIf(DestinationParser::isAvailable)?.let {
                action
                    ?.takeIf {
                        it == IncomingLaunchAction.SEARCH || it == IncomingLaunchAction.FOCUS_SEARCH ||
                            it == IncomingLaunchAction.RANDOM_WORD
                    }?.let { incomingAction ->
                        IncomingActionSignal.publish(
                            IncomingActionEvent(
                                request.requestId,
                                incomingAction,
                                sharedWord,
                            ),
                        )
                    }
                navigator.openExternal(it)
                onAccepted()
            }
            requests.acknowledge(request.requestId)
        }
    }
}
