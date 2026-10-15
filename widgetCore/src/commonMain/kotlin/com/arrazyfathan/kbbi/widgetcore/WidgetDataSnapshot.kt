package com.arrazyfathan.kbbi.widgetcore

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WidgetDataSnapshot(
    val schemaVersion: Int = WidgetSnapshot.CURRENT_SCHEMA_VERSION,
    val revision: Long,
    val languageTag: String,
    val catalogWords: List<String>,
    val savedWords: List<String>,
) {
    companion object {
        const val MAX_ENCODED_BYTES = 2_000_000
    }
}

object WidgetDataSnapshotCodec {
    private val json = Json { encodeDefaults = true }

    fun encode(snapshot: WidgetDataSnapshot): String = json.encodeToString(snapshot)

    fun decode(encoded: String): WidgetDataSnapshot? {
        if (encoded.length > WidgetDataSnapshot.MAX_ENCODED_BYTES) return null
        return runCatching { json.decodeFromString<WidgetDataSnapshot>(encoded) }
            .getOrNull()
            ?.takeIf {
                it.schemaVersion == WidgetSnapshot.CURRENT_SCHEMA_VERSION && it.languageTag in
                    setOf(
                        "en",
                        "id",
                        "in",
                    ) && it.catalogWords.size <= MAX_WORD_COUNT && it.savedWords.size <= MAX_WORD_COUNT &&
                    (it.catalogWords + it.savedWords).all { word ->
                        word.length <= MAX_WORD_LENGTH && word.none(Char::isISOControl)
                    }
            }
    }

    private const val MAX_WORD_COUNT = 100_000
    private const val MAX_WORD_LENGTH = 256
}
