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
        pending.update { queue -> if (queue.size >= 32 || queue.any { it.requestId == request.requestId }) queue else queue + request }
    }

    fun parse(arguments: List<String>): IncomingLaunchRequest? {
        if (arguments.isEmpty()) return null
        val recognized = arguments.firstOrNull()?.startsWith("--") == true
        if (!recognized) {
            val value =
                arguments.singleOrNull()?.takeIf {
                    it.length <= 2048 && (
                        it.startsWith("kbbi://") || it.startsWith("https://") ||
                            it.startsWith(
                                "http://",
                            )
                    )
                } ?: return null
            return IncomingLaunchRequest(uri = value)
        }
        val requests = mutableListOf<IncomingLaunchRequest>()
        var index = 0
        while (index < arguments.size) {
            when (arguments[index]) {
                "--search", "--word" -> {
                    val value =
                        arguments
                            .getOrNull(index + 1)
                            ?.takeIf { it.isNotBlank() && it.length <= 4096 } ?: return null
                    requests += IncomingLaunchRequest(sharedText = value)
                    index += 2
                }

                "--bookmarks" -> {
                    requests += IncomingLaunchRequest(action = IncomingLaunchAction.OPEN_BOOKMARKS)
                    index++
                }

                "--proverbs" -> {
                    requests += IncomingLaunchRequest(action = IncomingLaunchAction.OPEN_PROVERBS)
                    index++
                }

                "--random-word" -> {
                    requests += IncomingLaunchRequest(action = IncomingLaunchAction.RANDOM_WORD)
                    index++
                }

                "--uri" -> {
                    val value =
                        arguments.getOrNull(index + 1)?.takeIf {
                            it.length <= 2048 && (
                                it.startsWith("kbbi://") || it.startsWith("https://") ||
                                    it.startsWith(
                                        "http://",
                                    )
                            )
                        } ?: return null
                    requests += IncomingLaunchRequest(uri = value)
                    index += 2
                }

                "--text-file" -> {
                    val path = arguments.getOrNull(index + 1) ?: return null
                    val file =
                        java.nio.file.Path
                            .of(path)
                    if (!java.nio.file.Files
                            .isRegularFile(file) || java.nio.file.Files
                            .size(file) > 16 * 1024
                    ) {
                        return null
                    }
                    val value =
                        java.nio.file.Files
                            .readString(file, Charsets.UTF_8)
                            .takeIf { it.length <= 4096 } ?: return null
                    requests += IncomingLaunchRequest(sharedText = value)
                    index += 2
                }

                else -> {
                    return null
                }
            }
        }
        return requests.singleOrNull()
    }
}
