package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class CachedTopWordEntity(
    val word: String,
    val visitorCount: Long,
    val position: Int,
)
