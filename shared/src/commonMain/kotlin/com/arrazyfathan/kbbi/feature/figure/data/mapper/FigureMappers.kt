package com.arrazyfathan.kbbi.feature.figure.data.mapper

import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigureDto
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigurePageDto
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePageModel
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

fun FigureDto.toDomain(): FigureModel =
    FigureModel(
        name = name?.stringValue()?.takeIf(String::isNotBlank),
        slug = slug,
        sourceUrl = sourceUrl,
        photo = photo?.stringValue()?.takeIf(String::isValidHttpUrl),
        description = description?.stringValue()?.takeIf(String::isNotBlank),
        quotes =
            (quotes as? JsonArray)
                ?.mapNotNull { element -> (element as? JsonPrimitive)?.takeIf { it.isString }?.content }
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                ?.takeIf { it.isNotEmpty() },
    )

fun FigurePageDto.toDomain(): FigurePageModel =
    FigurePageModel(
        items = items.map(FigureDto::toDomain),
        page = pagination.page,
        totalPages = pagination.totalPages,
        hasNextPage = pagination.hasNextPage,
    )

private fun String.isValidHttpUrl(): Boolean =
    (
        startsWith("https://", ignoreCase = true) ||
            startsWith(
                "http://",
                ignoreCase = true,
            )
    ) && substringAfter("://").substringBefore('/').isNotBlank()

private fun kotlinx.serialization.json.JsonElement.stringValue(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content
