package com.arrazyfathan.kbbi.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.platform.WidgetSnapshotPublisher
import com.arrazyfathan.kbbi.widgetcore.WidgetDataSnapshot
import kotlinx.coroutines.CancellationException

internal class AndroidWidgetSnapshotPublisher(
    private val context: Context,
) : WidgetSnapshotPublisher {
    override suspend fun publish(snapshot: WidgetDataSnapshot) {
        try {
            WordOfDayWidget().updateAll(context)
            SavedWordWidget().updateAll(context)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            AppLogger.error("WidgetRefresh", error, "Foreground widget refresh failed")
        }
    }
}
