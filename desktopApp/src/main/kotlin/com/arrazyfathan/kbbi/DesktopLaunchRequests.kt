package com.arrazyfathan.kbbi

import com.arrazyfathan.kbbi.core.platform.IncomingLaunchAction
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.nio.file.Files
import java.nio.file.Path

internal object DesktopLaunchRequests : IncomingLaunchRequests {
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

    fun publish(request: IncomingLaunchRequest) {
        pending.update { queue ->
            if (queue.size >= MAX_PENDING_REQUESTS || queue.any { it.requestId == request.requestId }) {
                queue
            } else {
                queue + request
            }
        }
    }

    fun parse(arguments: List<String>): IncomingLaunchRequest? =
        when (arguments.size) {
            1 -> parseSingleArgument(arguments.single())
            2 -> parseOption(arguments[0], arguments[1])
            else -> null
        }

    private fun parseSingleArgument(argument: String): IncomingLaunchRequest? =
        when (argument) {
            "--bookmarks" -> IncomingLaunchRequest(action = IncomingLaunchAction.OPEN_BOOKMARKS)
            "--proverbs" -> IncomingLaunchRequest(action = IncomingLaunchAction.OPEN_PROVERBS)
            "--random-word" -> IncomingLaunchRequest(action = IncomingLaunchAction.RANDOM_WORD)
            else -> parseUri(argument)
        }

    private fun parseOption(
        option: String,
        value: String,
    ): IncomingLaunchRequest? =
        when (option) {
            "--search", "--word" ->
                value
                    .takeIf { it.isNotBlank() && it.length <= MAX_TEXT_LENGTH }
                    ?.let { IncomingLaunchRequest(sharedText = it) }
            "--uri" -> parseUri(value)
            "--text-file" -> parseTextFile(value)
            else -> null
        }

    private fun parseUri(value: String): IncomingLaunchRequest? =
        value
            .takeIf { uri -> uri.length <= MAX_URI_LENGTH && URI_PREFIXES.any { uri.startsWith(it) } }
            ?.let { IncomingLaunchRequest(uri = it) }

    private fun parseTextFile(path: String): IncomingLaunchRequest? {
        val file = Path.of(path)
        if (!Files.isRegularFile(file) || Files.size(file) > MAX_TEXT_FILE_BYTES) return null
        return Files
            .readString(file, Charsets.UTF_8)
            .takeIf { it.length <= MAX_TEXT_LENGTH }
            ?.let { IncomingLaunchRequest(sharedText = it) }
    }

    private const val MAX_PENDING_REQUESTS = 32
    private const val MAX_URI_LENGTH = 2048
    private const val MAX_TEXT_LENGTH = 4096
    private const val MAX_TEXT_FILE_BYTES = 16 * 1024
    private val URI_PREFIXES = listOf("kbbi://", "https://", "http://")
}
