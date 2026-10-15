package com.arrazyfathan.kbbi.core.platform

import com.arrazyfathan.kbbi.widgetcore.WidgetDataSnapshot
import com.arrazyfathan.kbbi.widgetcore.WidgetDataSnapshotCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSNotificationCenter

internal object IosWidgetSnapshotPublisher : WidgetSnapshotPublisher {
    override suspend fun publish(snapshot: WidgetDataSnapshot) {
        val encoded = WidgetDataSnapshotCodec.encode(snapshot)
        withContext(Dispatchers.Main) {
            NSNotificationCenter.defaultCenter.postNotificationName(
                aName = WIDGET_SNAPSHOT_NOTIFICATION,
                `object` = encoded,
            )
        }
    }

    private const val WIDGET_SNAPSHOT_NOTIFICATION = "com.arrazyfathan.kbbi.widgetSnapshotUpdated"
}
