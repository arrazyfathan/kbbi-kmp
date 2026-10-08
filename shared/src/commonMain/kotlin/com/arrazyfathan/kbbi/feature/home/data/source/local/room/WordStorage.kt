package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import kotlinx.coroutines.flow.Flow

interface WordStorage {
    fun getItem(key: String): String?

    fun setItem(key: String, value: String)
}

enum class StorageOperation {
    Read,
    Write,
}

data class StorageIssue(
    val key: String,
    val operation: StorageOperation,
    val reason: String,
)

interface StorageIssueSource {
    fun getStorageIssues(): Flow<List<StorageIssue>>
}
