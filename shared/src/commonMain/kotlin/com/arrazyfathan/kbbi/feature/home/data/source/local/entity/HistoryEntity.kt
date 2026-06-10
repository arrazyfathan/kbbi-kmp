package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
expect fun currentTimeMillis(): Long

@Serializable
data class HistoryEntity(
    var word: String = "",
    val searchedAt: Long = currentTimeMillis(),
)
