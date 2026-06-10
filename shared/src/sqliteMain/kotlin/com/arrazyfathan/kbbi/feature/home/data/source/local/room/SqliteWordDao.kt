package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface RoomWordDao {
    @Query("SELECT * FROM word_table")
    fun getAllWords(): Flow<List<SqliteListWordEntity>>

    @Upsert
    suspend fun insertWord(entity: SqliteListWordEntity): Long

    @Query("DELETE FROM word_table WHERE TRIM(word) = TRIM(:word) COLLATE NOCASE")
    suspend fun deleteWord(word: String): Int

    @Query("SELECT EXISTS (SELECT * FROM word_table WHERE TRIM(word) = TRIM(:word) COLLATE NOCASE)")
    fun checkWordIsExist(word: String): Flow<Boolean>

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
}

class RoomWordDaoAdapter(
    private val dao: RoomWordDao,
) : WordDao {
    override fun getAllWords(): Flow<List<ListWordEntity>> =
        dao.getAllWords().map { entities -> entities.map(SqliteListWordEntity::toCommonEntity) }

    override suspend fun insertWord(listWordEntity: ListWordEntity): Long = dao.insertWord(listWordEntity.toSqliteEntity())

    override suspend fun deleteWord(word: String): Int = dao.deleteWord(word)

    override fun checkWordIsExist(word: String): Flow<Boolean> = dao.checkWordIsExist(word)

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
}
