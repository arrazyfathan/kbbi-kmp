package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import kotlin.test.Test
import kotlin.test.assertEquals

class MeaningTextTest {
    @Test
    fun removesBracketedWordClassAnnotations() {
        assertEquals("n Ling", cleanWordClass("n [cak] Ling"))
    }

    @Test
    fun removesDescriptionSuffixAfterQuestionMark() {
        assertEquals("meaning text", cleanMeaningDescription("meaning text?ignored metadata"))
    }
}
