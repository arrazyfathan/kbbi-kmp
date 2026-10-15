package com.arrazyfathan.kbbi.navigation

import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.widgetcore.WidgetKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DestinationParserTest {
    @Test
    fun validWidgetLinksPreserveTheCompleteSelectedWord() {
        assertEquals(
            WidgetKind.WORD_OF_DAY to "kata benda",
            DestinationParser.widgetLink("kbbi://widget/word-of-day/kata%20benda"),
        )
        assertEquals(
            WidgetKind.SAVED_WORD to "anak-anak",
            DestinationParser.widgetLink("#/widget/saved-word/anak-anak"),
        )
        assertEquals(WidgetKind.QUICK_SEARCH to null, DestinationParser.widgetLink("kbbi://widget/quick-search"))
    }

    @Test
    fun malformedUnknownAndOversizedLinksAreRejected() {
        assertNull(DestinationParser.widgetLink("kbbi://widget/unknown"))
        assertNull(DestinationParser.widgetLink("kbbi://widget/quick-search/unexpected"))
        assertNull(DestinationParser.widgetLink("kbbi://widget/word-of-day/bad%2Fword"))
        assertNull(DestinationParser.widgetLink("#/widget/word-of-day/bad%Q"))
        assertNull(DestinationParser.parse("https://example.test/${"a".repeat(2049)}"))
        assertNull(DestinationParser.parse("kbbi://word/${"a".repeat(257)}"))
    }

    @Test
    fun repeatedValidLaunchInputsResolveToTheSameDestination() {
        val link = "kbbi://word/kata%20benda"

        assertEquals(DetailKey("kata benda"), DestinationParser.parse(link))
        assertEquals(DestinationParser.parse(link), DestinationParser.parse(link))
    }
}
