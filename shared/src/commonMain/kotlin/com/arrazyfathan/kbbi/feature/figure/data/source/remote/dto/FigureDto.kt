package com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FigureDto(
    @SerialName("name") val name: JsonElement? = null,
    @SerialName("slug") val slug: String,
    @SerialName("sourceUrl") val sourceUrl: String,
    @SerialName("photo") val photo: JsonElement? = null,
    @SerialName("description") val description: JsonElement? = null,
    @SerialName("quotes") val quotes: JsonElement? = null,
)
