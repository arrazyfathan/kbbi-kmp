package com.arrazyfathan.kbbi.widgetcore

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WidgetSnapshotTest {
    @Test
    fun dailySelectionMatchesTheReminderSeedAndPreservesCatalogOrder() {
        val words = listOf("", "buku", "kata", "rumah")
        val seed = 2026 * 366 + 287
        val orderedWords = listOf("buku", "kata", "rumah")
        val reversedWords = orderedWords.reversed()

        assertEquals(orderedWords[Random(seed).nextInt(orderedWords.size)], WidgetDailyItemSelector.selectWordOfDay(words, 2026, 287))
        assertEquals(reversedWords[Random(seed).nextInt(reversedWords.size)], WidgetDailyItemSelector.selectWordOfDay(words.reversed(), 2026, 287))
    }

    @Test
    fun savedWordSelectionIsStableAcrossInputOrdering() {
        val first = WidgetDailyItemSelector.selectSavedWord(listOf("rumah", "buku", "kata"), 2026, 287)
        val reordered = WidgetDailyItemSelector.selectSavedWord(listOf("kata", "rumah", "buku"), 2026, 287)

        assertEquals(first, reordered)
    }

    @Test
    fun emptyInputsAndCorruptOrUnsupportedSnapshotsFailSafely() {
        assertNull(WidgetDailyItemSelector.selectWordOfDay(emptyList(), 2026, 287))
        assertNull(WidgetDailyItemSelector.selectSavedWord(listOf(" "), 2026, 287))
        assertNull(WidgetDailyItemSelector.decodeSnapshot("not-json"))
        assertNull(
            WidgetDailyItemSelector.decodeSnapshot(
                """{"schemaVersion":99,"kind":"WORD_OF_DAY","localDate":"2026-10-14","languageTag":"en","selectedWord":"kata"}""",
            ),
        )
        assertNull(WidgetDataSnapshotCodec.decode("x".repeat(WidgetDataSnapshot.MAX_ENCODED_BYTES + 1)))
    }
}
