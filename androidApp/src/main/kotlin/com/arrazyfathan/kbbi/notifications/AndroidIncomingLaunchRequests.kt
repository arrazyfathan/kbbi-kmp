package com.arrazyfathan.kbbi.notifications

import android.content.Intent
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AndroidIncomingLaunchRequests : IncomingLaunchRequests {
    private val pending =
        MutableSharedFlow<IncomingLaunchRequest>(
            replay = 1,
            extraBufferCapacity = 8,
        )

    override fun observe() = pending.asSharedFlow()

    fun publish(intent: Intent?) {
        intent?.dataString?.let {
            pending.tryEmit(
                IncomingLaunchRequest(
                    uri = it,
                    requestId = "notification-${it.hashCode()}",
                ),
            )
        }
    }
}
