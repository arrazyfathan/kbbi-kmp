package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

@Serializable
data class ListWordEntity(
    val word: String = "",
    val listWords: List<WordEntity>,
    val visitorCount: Int? = null,
    var isSaved: Boolean = false,
)
