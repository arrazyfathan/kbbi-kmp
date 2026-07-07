package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity

class InMemoryProverbDao : ProverbDao {
    private val proverbs = mutableListOf<CachedProverbEntity>()
    private val details = mutableMapOf<String, CachedProverbDetailEntity>()

    override suspend fun getProverbs(
        query: String,
        page: Int,
    ): List<CachedProverbEntity> =
        proverbs
            .filter { it.query == query && it.page == page }
            .sortedBy { it.position }

    override suspend fun replaceProverbPage(
        query: String,
        page: Int,
        proverbs: List<CachedProverbEntity>,
    ) {
        if (page == FIRST_PROVERB_PAGE) {
            this.proverbs.removeAll { it.query == query }
        } else {
            this.proverbs.removeAll { it.query == query && it.page == page }
        }
        this.proverbs.addAll(proverbs)
    }

    override suspend fun getProverbDetail(slug: String): CachedProverbDetailEntity? = details[slug]

    override suspend fun upsertProverbDetail(proverb: CachedProverbDetailEntity) {
        details[proverb.slug] = proverb
    }

    private companion object {
        const val FIRST_PROVERB_PAGE = 1
    }
}
