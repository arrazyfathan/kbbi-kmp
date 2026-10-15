package com.arrazyfathan.kbbi.core.platform

import com.arrazyfathan.kbbi.widgetcore.WidgetDataSnapshot

/** Publishes minimal word-index/bookmark data for native glanceable surfaces. */
fun interface WidgetSnapshotPublisher {
    suspend fun publish(snapshot: WidgetDataSnapshot)
}

object NoOpWidgetSnapshotPublisher : WidgetSnapshotPublisher {
    override suspend fun publish(snapshot: WidgetDataSnapshot) = Unit
}
