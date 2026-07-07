package com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity

data class CachedProverbDetailEntity(
    val slug: String,
    val text: String,
    val letter: String,
    val sourceUrl: String?,
    val meaning: String?,
)
