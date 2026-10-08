package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.CachedTopWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface RoomWordDao {
    @Query("SELECT * FROM word_table")
    fun getAllWords(): Flow<List<SqliteListWordEntity>>

    @Query("SELECT * FROM word_table WHERE isSaved = 1")
    fun getSavedWords(): Flow<List<SqliteListWordEntity>>

    @Query("SELECT * FROM word_table WHERE TRIM(word) = TRIM(:word) COLLATE NOCASE LIMIT 1")
    suspend fun getWord(word: String): SqliteListWordEntity?

    @Upsert
    suspend fun insertWord(entity: SqliteListWordEntity): Long

    @Query("UPDATE word_table SET isSaved = 0 WHERE TRIM(word) = TRIM(:word) COLLATE NOCASE AND isSaved = 1")
    suspend fun deleteWord(word: String): Int

    @Query("SELECT EXISTS (SELECT * FROM word_table WHERE TRIM(word) = TRIM(:word) COLLATE NOCASE AND isSaved = 1)")
    fun checkWordIsSaved(word: String): Flow<Boolean>

    @Upsert
    suspend fun insertHistory(entity: SqliteHistoryEntity)

    @Query(
        "DELETE FROM history_table WHERE word NOT IN (SELECT word FROM history_table ORDER BY searchedAt DESC, word DESC LIMIT :limit)",
    )
    suspend fun trimHistories(limit: Int)

    @Transaction
    suspend fun insertHistoryAndTrim(
        entity: SqliteHistoryEntity,
        limit: Int = 5,
    ) {
        insertHistory(entity)
        trimHistories(limit)
    }

    @Query("SELECT * FROM history_table ORDER BY searchedAt DESC, word DESC")
    fun getListHistory(): Flow<List<SqliteHistoryEntity>>

    @Query("DELETE FROM history_table")
    suspend fun clearHistory()

    @Query("SELECT * FROM cached_top_word_table ORDER BY position ASC")
    suspend fun getTopWords(): List<SqliteTopWordCacheEntity>

    @Upsert
    suspend fun upsertTopWords(topWords: List<SqliteTopWordCacheEntity>)

    @Query("DELETE FROM cached_top_word_table")
    suspend fun clearTopWords()

    @Transaction
    suspend fun replaceTopWords(topWords: List<SqliteTopWordCacheEntity>) {
        clearTopWords()
        if (topWords.isNotEmpty()) upsertTopWords(topWords)
    }
}

class RoomWordDaoAdapter(
    private val dao: RoomWordDao,
) : WordDao {
    override fun getAllWords(): Flow<List<ListWordEntity>> =
        dao.getAllWords().map { entities -> entities.map(SqliteListWordEntity::toCommonEntity) }

    override fun getSavedWords(): Flow<List<ListWordEntity>> =
        dao.getSavedWords().map { entities -> entities.map(SqliteListWordEntity::toCommonEntity) }

    override suspend fun getWord(word: String): ListWordEntity? = dao.getWord(word)?.toCommonEntity()

    override suspend fun insertWord(listWordEntity: ListWordEntity): Long = dao.insertWord(listWordEntity.toSqliteEntity())

    override suspend fun deleteWord(word: String): Int = dao.deleteWord(word)

    override fun checkWordIsSaved(word: String): Flow<Boolean> = dao.checkWordIsSaved(word)

    override suspend fun insertHistory(historyEntity: HistoryEntity) {
        dao.insertHistory(historyEntity.toSqliteEntity())
    }

    override suspend fun trimHistories(limit: Int) {
        dao.trimHistories(limit)
    }

    override suspend fun insertHistoryAndTrim(
        historyEntity: HistoryEntity,
        limit: Int,
    ) {
        dao.insertHistoryAndTrim(historyEntity.toSqliteEntity(), limit)
    }

    override fun getListHistory(): Flow<List<HistoryEntity>> =
        dao.getListHistory().map { entities -> entities.map(SqliteHistoryEntity::toCommonEntity) }

    override suspend fun clearHistory() = dao.clearHistory()

    override suspend fun getTopWords(): List<CachedTopWordEntity> = dao.getTopWords().map(SqliteTopWordCacheEntity::toCommonEntity)

    override suspend fun replaceTopWords(topWords: List<CachedTopWordEntity>) {
        dao.replaceTopWords(topWords.map(CachedTopWordEntity::toSqliteEntity))
    }
}
