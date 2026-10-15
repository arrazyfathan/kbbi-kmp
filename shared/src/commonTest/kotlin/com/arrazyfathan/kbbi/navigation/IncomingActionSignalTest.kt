package com.arrazyfathan.kbbi.navigation

import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IncomingActionSignalTest {
    @AfterTest
    fun clearPendingEvents() {
        IncomingActionSignal.pending.value.forEach { IncomingActionSignal.consume(it.requestId) }
    }

    @Test
    fun repeatedRequestIdIsQueuedOnceAndCanBeReusedAfterConsumption() {
        val request = IncomingActionEvent("same-request", IncomingLaunchAction.SEARCH, "kata")

        IncomingActionSignal.publish(request)
        IncomingActionSignal.publish(request)
        assertEquals(listOf(request), IncomingActionSignal.pending.value)

        IncomingActionSignal.consume(request.requestId)
        IncomingActionSignal.publish(request)
        assertEquals(listOf(request), IncomingActionSignal.pending.value)
    }
}
