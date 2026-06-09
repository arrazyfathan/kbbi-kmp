package com.arrazyfathan.kbbi.core.presentation.designsystem

import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.close
import kbbi_kmp.shared.generated.resources.copy
import kbbi_kmp.shared.generated.resources.home
import kbbi_kmp.shared.generated.resources.home_selected
import kbbi_kmp.shared.generated.resources.ic_delete
import kbbi_kmp.shared.generated.resources.ic_history
import kbbi_kmp.shared.generated.resources.ic_search
import kbbi_kmp.shared.generated.resources.saved
import kbbi_kmp.shared.generated.resources.saved_selected
import kbbi_kmp.shared.generated.resources.word
import kbbi_kmp.shared.generated.resources.word_selected
import org.jetbrains.compose.resources.DrawableResource

object KBBIIcons {
    val home: DrawableResource = Res.drawable.home
    val homeSelected: DrawableResource = Res.drawable.home_selected
    val words: DrawableResource = Res.drawable.word
    val wordsSelected: DrawableResource = Res.drawable.word_selected
    val bookmarks: DrawableResource = Res.drawable.saved
    val bookmarksSelected: DrawableResource = Res.drawable.saved_selected
    val search: DrawableResource = Res.drawable.ic_search
    val history: DrawableResource = Res.drawable.ic_history
    val delete: DrawableResource = Res.drawable.ic_delete
    val close: DrawableResource = Res.drawable.close
    val copy: DrawableResource = Res.drawable.copy
}
