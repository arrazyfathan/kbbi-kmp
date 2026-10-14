package com.arrazyfathan.kbbi.navigation

import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object IosIncomingLaunchRequests : IncomingLaunchRequests {
    private val pending =
        MutableSharedFlow<IncomingLaunchRequest>(replay = 1, extraBufferCapacity = 8)

    override fun observe() = pending.asSharedFlow()

    fun publish(uri: String) {
        pending.tryEmit(
            IncomingLaunchRequest(
                uri = uri,
                requestId = "notification-${uri.hashCode()}",
            ),
        )
    }
}
