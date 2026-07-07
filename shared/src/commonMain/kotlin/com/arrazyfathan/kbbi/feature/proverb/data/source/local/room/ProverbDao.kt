package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity

interface ProverbDao {
    suspend fun getProverbs(
        query: String,
        page: Int,
    ): List<CachedProverbEntity>

    suspend fun replaceProverbPage(
        query: String,
        page: Int,
        proverbs: List<CachedProverbEntity>,
    )

    suspend fun getProverbDetail(slug: String): CachedProverbDetailEntity?

    suspend fun upsertProverbDetail(proverb: CachedProverbDetailEntity)
}
