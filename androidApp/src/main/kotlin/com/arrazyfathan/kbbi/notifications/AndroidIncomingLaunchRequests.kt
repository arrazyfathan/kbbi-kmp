package com.arrazyfathan.kbbi.notifications

import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.widgetcore.WidgetKind
import com.arrazyfathan.kbbi.widgets.EXTRA_WIDGET_WORD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object AndroidIncomingLaunchRequests : IncomingLaunchRequests {
    private val pending = MutableStateFlow<List<IncomingLaunchRequest>>(emptyList())

    override fun observe() =
        pending
            .asStateFlow()
            .map { it.firstOrNull() }
            .filterNotNull()
            .distinctUntilChangedBy { it.requestId }

    override fun acknowledge(requestId: String) {
        pending.update { queue -> queue.filterNot { it.requestId == requestId } }
    }

    fun publish(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val widgetWord = intent.getStringExtra(EXTRA_WIDGET_WORD)
        val text = intent.sharedText()?.take(SHARED_TEXT_LIMIT) ?: widgetWord?.take(WIDGET_TEXT_LIMIT)
        val shortcut = shortcutFor(action, widgetWord)
        val uri = intent.launchUri()?.takeIf { it.length <= URI_LIMIT }
        if (text == null && shortcut == null && uri == null) return

        val widgetKind = widgetKindFor(action)
        val request =
            IncomingLaunchRequest(
                uri = uri,
                sharedText = text,
                action = shortcut,
                widgetKind = widgetKind,
                widgetWord =
                    text.takeIf {
                        widgetKind == WidgetKind.WORD_OF_DAY || widgetKind == WidgetKind.SAVED_WORD
                    },
                requestId = intent.launchRequestId(),
            )
        pending.update { queue ->
            if (queue.size >= MAX_PENDING_REQUESTS || queue.any { it.requestId == request.requestId }) {
                queue
            } else {
                queue + request
            }
        }
    }

    private fun Intent.sharedText(): String? =
        when (action) {
            Intent.ACTION_SEND -> {
                if (type?.startsWith("text/plain") == true) {
                    getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
                } else {
                    null
                }
            }
            Intent.ACTION_PROCESS_TEXT -> getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            else -> null
        }

    private fun shortcutFor(
        action: String?,
        widgetWord: String?,
    ): IncomingLaunchAction? =
        when (action) {
            ACTION_SHORTCUT_SEARCH, ACTION_WIDGET_QUICK_SEARCH -> IncomingLaunchAction.FOCUS_SEARCH
            ACTION_SHORTCUT_BOOKMARKS -> IncomingLaunchAction.OPEN_BOOKMARKS
            ACTION_SHORTCUT_PROVERBS -> IncomingLaunchAction.OPEN_PROVERBS
            ACTION_SHORTCUT_RANDOM_WORD -> IncomingLaunchAction.RANDOM_WORD
            ACTION_WIDGET_WORD_OF_DAY -> IncomingLaunchAction.SEARCH
            ACTION_WIDGET_SAVED_WORD -> {
                if (widgetWord.isNullOrBlank()) IncomingLaunchAction.OPEN_BOOKMARKS else IncomingLaunchAction.SEARCH
            }
            else -> null
        }

    private fun Intent.launchUri(): String? =
        dataString ?: getStringExtra("url")
            ?: getStringExtra("editorial_destination_value")?.let { value ->
                when (getStringExtra("editorial_destination_kind")) {
                    "word" -> "kbbi://word/$value"
                    "proverb" -> "kbbi://proverb/$value"
                    else -> null
                }
            }

    private fun widgetKindFor(action: String?): WidgetKind? =
        when (action) {
            ACTION_WIDGET_QUICK_SEARCH -> WidgetKind.QUICK_SEARCH
            ACTION_WIDGET_WORD_OF_DAY -> WidgetKind.WORD_OF_DAY
            ACTION_WIDGET_SAVED_WORD -> WidgetKind.SAVED_WORD
            else -> null
        }

    private fun Intent.launchRequestId(): String {
        val suppliedId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) identifier else null
        return suppliedId ?: "android-${SystemClock.elapsedRealtimeNanos()}"
    }

    private const val ACTION_SHORTCUT_SEARCH = "com.arrazyfathan.kbbi.action.SHORTCUT_SEARCH"
    private const val ACTION_SHORTCUT_BOOKMARKS = "com.arrazyfathan.kbbi.action.SHORTCUT_BOOKMARKS"
    private const val ACTION_SHORTCUT_PROVERBS = "com.arrazyfathan.kbbi.action.SHORTCUT_PROVERBS"
    private const val ACTION_SHORTCUT_RANDOM_WORD =
        "com.arrazyfathan.kbbi.action.SHORTCUT_RANDOM_WORD"
    private const val ACTION_WIDGET_QUICK_SEARCH =
        "com.arrazyfathan.kbbi.action.WIDGET_QUICK_SEARCH"
    private const val ACTION_WIDGET_WORD_OF_DAY = "com.arrazyfathan.kbbi.action.WIDGET_WORD_OF_DAY"
    private const val ACTION_WIDGET_SAVED_WORD = "com.arrazyfathan.kbbi.action.WIDGET_SAVED_WORD"
    private const val MAX_PENDING_REQUESTS = 32
    private const val SHARED_TEXT_LIMIT = 4097
    private const val WIDGET_TEXT_LIMIT = 257
    private const val URI_LIMIT = 2049
}
