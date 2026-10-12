package com.arrazyfathan.kbbi.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Only identifiers are persisted; invalid or obsolete saved state falls back to its root. */
internal object NavigationCodec {
    private val json =
        Json {
            serializersModule = navigationSavedStateConfiguration.serializersModule
            allowStructuredMapKeys = true
        }
    private val stackSerializer = ListSerializer(PolymorphicSerializer(NavKey::class))
    private const val MAX_SAVED_STATE_LENGTH = 65536

    fun encode(snapshot: NavigationSnapshot): String = json.encodeToString(NavigationSnapshot.serializer(), snapshot)

    fun decode(value: String): NavigationSnapshot? =
        value.takeIf { it.length <= MAX_SAVED_STATE_LENGTH }?.let {
            runCatching { json.decodeFromString(NavigationSnapshot.serializer(), it) }
                .getOrNull()
                ?.takeIf { it.isValid() }
        }

    fun encodeStack(stack: List<NavKey>): String = json.encodeToString(stackSerializer, stack)

    fun decodeStack(
        value: String,
        root: NavKey,
    ): List<NavKey> =
        value
            .takeIf { it.length <= MAX_SAVED_STATE_LENGTH }
            ?.let {
                runCatching { json.decodeFromString(stackSerializer, it) }.getOrNull()
            }?.takeIf { stack ->
                stack.isNotEmpty() && stack.size <= 32 && stack.first() == root &&
                    stack.all(
                        DestinationParser::isAvailable,
                    ) && stack.drop(1).none { it in topLevelRoutes }
            } ?: listOf(root)
}
