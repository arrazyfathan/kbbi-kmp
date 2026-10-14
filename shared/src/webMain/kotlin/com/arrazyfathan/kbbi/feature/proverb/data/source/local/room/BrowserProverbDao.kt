package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import web.storage.localStorage

/** Persists the proverb page cache so reminders can select cached content after a browser restart. */
class BrowserProverbDao(
    private val json: Json,
) : ProverbDao {
    private val memory = InMemoryProverbDao()

    override suspend fun getCachedProverbs(): List<CachedProverbEntity> =
        runCatching {
            json.decodeFromString(
                ListSerializer(CachedProverbEntity.serializer()),
                localStorage.getItem(CACHE_KEY).orEmpty(),
            )
        }.getOrElse { memory.getCachedProverbs() }

    override suspend fun getProverbs(
        query: String,
        page: Int,
    ): List<CachedProverbEntity> = memory.getProverbs(query, page)

    override suspend fun replaceProverbPage(
        query: String,
        page: Int,
        proverbs: List<CachedProverbEntity>,
    ) {
        memory.replaceProverbPage(query, page, proverbs)
        val previous =
            getCachedProverbs().filterNot { it.query == query && (page == FIRST_PAGE || it.page == page) }
        val cached =
            (previous + proverbs)
                .distinctBy { "${it.query}:${it.page}:${it.slug}" }
                .sortedWith(compareBy({ it.slug }, { it.query }, { it.page }))
        runCatching {
            localStorage.setItem(
                CACHE_KEY,
                json.encodeToString(ListSerializer(CachedProverbEntity.serializer()), cached),
            )
        }
    }

    override suspend fun getProverbDetail(slug: String): CachedProverbDetailEntity? = memory.getProverbDetail(slug)

    override suspend fun upsertProverbDetail(proverb: CachedProverbDetailEntity) = memory.upsertProverbDetail(proverb)

    private companion object {
        const val CACHE_KEY = "kbbi.proverb.reminder-cache.v1"
        const val FIRST_PAGE = 1
    }
}
