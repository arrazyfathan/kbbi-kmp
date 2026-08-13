package com.arrazyfathan.kbbi.feature.home.data

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.map
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.mapper.toEntity
import com.arrazyfathan.kbbi.feature.home.data.mapper.toHistoryModels
import com.arrazyfathan.kbbi.feature.home.data.mapper.toWordEntities
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordResultModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordRepository(
    private val remoteDataSource: WordRemoteDataSource,
    private val localDataSource: WordLocalDataSource,
) : WordSearchRepository,
    BookmarkRepository,
    SearchHistoryRepository {
    override suspend fun getMeaningOfWord(word: String): AppResult<WordResultModel, DataError> =
        remoteDataSource.getMeaningOfWord(word).map { it.toDomain() }

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int?,
    ): Boolean =
        withContext(Dispatchers.Default) {
            val normalizedWord = word.trim().lowercase()
            localDataSource.insertWord(
                ListWordEntity(
                    word = normalizedWord,
                    listWords = result.toWordEntities(),
                    visitorCount = visitorCount,
                    isSaved = true,
                ),
            ) != -1L
        }

    override suspend fun addToHistory(history: HistoryModel) =
        withContext(Dispatchers.Default) {
            return@withContext localDataSource.insertHistory(history.toEntity())
        }

    override fun getAllHistories(): Flow<List<HistoryModel>> =
        localDataSource.getAllHistories().map {
            it.toHistoryModels()
        }

    override suspend fun deleteWord(word: String): Boolean =
        withContext(Dispatchers.Default) {
            localDataSource.deleteWord(word.trim().lowercase()) > 0
        }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = localDataSource.checkWordIsExist(word.trim().lowercase())

    override fun getBookmarks(): Flow<List<ListWordModel>> =
        localDataSource.getAllWords().map {
            it.map { entity -> entity.toDomain() }
        }
}
