package com.arrazyfathan.kbbi.notifications

import android.content.Intent
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
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
        val sharedText =
            when (action) {
                Intent.ACTION_SEND -> {
                    if (intent.type?.startsWith("text/plain") == true) {
                        intent
                            .getCharSequenceExtra(
                                Intent.EXTRA_TEXT,
                            )?.toString()
                    } else {
                        null
                    }
                }

                Intent.ACTION_PROCESS_TEXT -> {
                    intent
                        .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
                        ?.toString()
                }

                else -> {
                    null
                }
            }
        val shortcut =
            when (action) {
                ACTION_SHORTCUT_SEARCH -> IncomingLaunchAction.FOCUS_SEARCH
                ACTION_SHORTCUT_BOOKMARKS -> IncomingLaunchAction.OPEN_BOOKMARKS
                ACTION_SHORTCUT_PROVERBS -> IncomingLaunchAction.OPEN_PROVERBS
                ACTION_SHORTCUT_RANDOM_WORD -> IncomingLaunchAction.RANDOM_WORD
                ACTION_WIDGET_QUICK_SEARCH -> IncomingLaunchAction.FOCUS_SEARCH
                ACTION_WIDGET_WORD_OF_DAY -> IncomingLaunchAction.SEARCH
                ACTION_WIDGET_SAVED_WORD -> IncomingLaunchAction.OPEN_BOOKMARKS
                else -> null
            }
        val uri =
            intent.dataString ?: intent.getStringExtra("url")
                ?: intent.getStringExtra("editorial_destination_value")?.let { value ->
                    when (intent.getStringExtra("editorial_destination_kind")) {
                        "word" -> "kbbi://word/$value"
                        "proverb" -> "kbbi://proverb/$value"
                        else -> null
                    }
                }
        val boundedText =
            sharedText?.take(4097) ?: intent.getStringExtra(EXTRA_WIDGET_WORD)?.take(257)
        val boundedUri = uri?.takeIf { it.length <= 2049 }
        if (boundedText == null && shortcut == null && boundedUri == null) return
        val request =
            IncomingLaunchRequest(
                uri = boundedUri,
                sharedText = boundedText,
                action = shortcut,
                requestId =
                    intent.identifier
                        ?: "android-${android.os.SystemClock.elapsedRealtimeNanos()}",
            )
        pending.update { queue ->
            if (queue.size >= MAX_PENDING_REQUESTS || queue.any { it.requestId == request.requestId }) queue else queue + request
        }
    }

    private const val EXTRA_WIDGET_WORD = "com.arrazyfathan.kbbi.extra.WIDGET_WORD"
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
}
