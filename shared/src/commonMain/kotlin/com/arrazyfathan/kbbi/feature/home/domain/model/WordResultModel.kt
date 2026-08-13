package com.arrazyfathan.kbbi.feature.home.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class WordResultModel(
    val word: String,
    val entries: List<WordModel>,
    val visitorCount: Int? = null,
)
