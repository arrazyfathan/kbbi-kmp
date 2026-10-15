package com.arrazyfathan.kbbi.feature.widgets

import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.platform.WidgetSnapshotPublisher
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.widgetcore.WidgetDataSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Runs in the host composition's coroutine so cancellation follows the app lifecycle. */
class WidgetSyncCoordinator(
    private val bookmarks: BookmarkRepository,
    private val contentProvider: WidgetContentProvider,
    private val publisher: WidgetSnapshotPublisher,
) {
    suspend fun run(languageTag: String) {
        val catalogWords = contentProvider.catalogWords()
        var revision = 0L
        bookmarks
            .getBookmarks()
            .map { saved -> saved.map { it.word } }
            .distinctUntilChanged()
            .collect { savedWords ->
                try {
                    publisher.publish(
                        WidgetDataSnapshot(
                            revision = ++revision,
                            languageTag = languageTag,
                            catalogWords = catalogWords,
                            savedWords = savedWords,
                        ),
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    AppLogger.error("WidgetSync", error, "Widget snapshot publish failed")
                }
            }
    }
}
