package com.arrazyfathan.kbbi.feature.bookmark.domain

enum class BookmarkLayout(
    val storageKey: String,
) {
    GRID("grid"),
    LIST("list"),
    ;

    companion object {
        fun fromStorageKey(key: String?): BookmarkLayout = entries.firstOrNull { it.storageKey == key } ?: GRID
    }
}
