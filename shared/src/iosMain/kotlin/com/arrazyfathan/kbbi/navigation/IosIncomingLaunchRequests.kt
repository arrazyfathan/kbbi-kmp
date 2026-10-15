package com.arrazyfathan.kbbi.navigation

import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import platform.Foundation.NSUserDefaults

object IosIncomingLaunchRequests : IncomingLaunchRequests {
    private val pending = MutableStateFlow<List<IncomingLaunchRequest>>(emptyList())

    override fun observe() =
        pending
            .asStateFlow()
            .map { it.firstOrNull() }
            .filterNotNull()
            .distinctUntilChangedBy { it.requestId }

    override fun acknowledge(requestId: String) {
        var acknowledgedRequest: IncomingLaunchRequest? = null
        pending.update { queue ->
            acknowledgedRequest = queue.firstOrNull { it.requestId == requestId }
            queue.filterNot { it.requestId == requestId }
        }
        if (acknowledgedRequest?.requestId?.startsWith(SHARE_REQUEST_PREFIX) == true) {
            NSUserDefaults(suiteName = APP_GROUP).apply {
                removeObjectForKey(PENDING_SHARE_TEXT_KEY)
                removeObjectForKey(PENDING_SHARE_ID_KEY)
                synchronize()
            }
        }
    }

    fun publish(uri: String) {
        enqueue(
            IncomingLaunchRequest(
                uri = uri.take(2049),
                requestId = "ios-${kotlin.random.Random.nextLong()}",
            ),
        )
    }

    fun publishText(sharedText: String) {
        enqueue(
            IncomingLaunchRequest(
                sharedText = sharedText.take(4097),
                requestId = "$SHARE_REQUEST_PREFIX${
                    NSUserDefaults(suiteName = APP_GROUP).stringForKey(PENDING_SHARE_ID_KEY) ?: kotlin.random.Random
                        .nextLong()
                        .toString()
                }",
            ),
        )
    }

    private fun enqueue(request: IncomingLaunchRequest) {
        pending.update { queue ->
            if (queue.size >= MAX_PENDING_REQUESTS || queue.any { it.requestId == request.requestId }) queue else queue + request
        }
    }

    private const val MAX_PENDING_REQUESTS = 32
    private const val SHARE_REQUEST_PREFIX = "ios-share-"
    private const val APP_GROUP = "group.com.arrazyfathan.kbbi"
    private const val PENDING_SHARE_TEXT_KEY = "pendingShareText"
    private const val PENDING_SHARE_ID_KEY = "pendingShareId"
}
