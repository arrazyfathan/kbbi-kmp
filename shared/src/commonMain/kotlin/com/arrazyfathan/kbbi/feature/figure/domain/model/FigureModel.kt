package com.arrazyfathan.kbbi.feature.figure.domain.model

import androidx.compose.runtime.Stable

@Stable
data class FigureModel(
    val name: String?,
    val slug: String,
    val sourceUrl: String,
    val photo: String? = null,
    val description: String? = null,
    val quotes: List<String>? = null,
)
