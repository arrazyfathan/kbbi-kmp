package com.arrazyfathan.kbbi.feature.home.data.source.local

import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import kbbi_kmp.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

class AssetWordCatalogRepository(
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : WordCatalogRepository {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getWords(): List<String> =
        withContext(ioDispatcher) {
            val jsonString =
                try {
                    Res.readBytes("files/entries.json").decodeToString()
                } catch (_: Exception) {
                    return@withContext emptyList()
                }

            json.decodeFromString<List<String>>(jsonString)
        }
}
