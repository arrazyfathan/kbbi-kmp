package com.arrazyfathan.kbbi.navigation

import androidx.navigation3.runtime.NavKey
import com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation.BookmarksKey
import com.arrazyfathan.kbbi.feature.detail.presentation.navigation.DetailKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureDetailKey
import com.arrazyfathan.kbbi.feature.figure.presentation.navigation.FigureKey
import com.arrazyfathan.kbbi.feature.home.presentation.navigation.HomeKey
import com.arrazyfathan.kbbi.feature.proverb.presentation.navigation.ProverbKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.LegalDocumentKey
import com.arrazyfathan.kbbi.feature.settings.presentation.navigation.SettingsKey
import com.arrazyfathan.kbbi.feature.words.presentation.navigation.WordsKey
import com.arrazyfathan.kbbi.widgetcore.WidgetKind
import io.ktor.http.Url
import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLPathPart

/** The same bounded parser handles browser routes and native kbbi:// links. */
internal object DestinationParser {
    private const val MAX_LINK_LENGTH = 2048
    private const val MAX_ARGUMENT_LENGTH = 256

    fun parse(input: String): NavKey? {
        if (input.length > MAX_LINK_LENGTH || input.any { it.isISOControl() }) return null
        val raw = input.trim()
        if (raw.isEmpty()) return HomeKey
        widgetLink(raw)?.let { (kind, word) ->
            return when {
                kind == WidgetKind.SAVED_WORD && word == null -> BookmarksKey
                else -> HomeKey
            }
        }
        val path =
            when {
                raw.startsWith("#/") -> {
                    raw.drop(1)
                }

                raw.startsWith("/") -> {
                    raw
                }

                else -> {
                    val url = runCatching { Url(raw) }.getOrNull() ?: return null
                    if (!url.user.isNullOrEmpty() || !url.password.isNullOrEmpty() ||
                        url.parameters
                            .names()
                            .isNotEmpty()
                    ) {
                        return null
                    }
                    when (url.protocol.name.lowercase()) {
                        "kbbi" -> "/${url.host}${url.encodedPath}"
                        "http", "https" -> url.fragment.takeIf { it.startsWith("/") } ?: url.encodedPath
                        else -> return null
                    }
                }
            }
        if ('?' in path || '#' in path || path.startsWith("//")) return null
        val pieces = path.removePrefix("/").trimEnd('/').split('/')
        if (pieces.size > 2) return null
        val name = pieces[0].lowercase()
        val argument =
            pieces.getOrNull(1)?.let { encoded ->
                if (encoded.length > MAX_ARGUMENT_LENGTH * 3 ||
                    Regex("%(?![0-9a-fA-F]{2})").containsMatchIn(
                        encoded,
                    )
                ) {
                    return null
                }
                val decoded = runCatching { encoded.decodeURLPart() }.getOrNull() ?: return null
                if (!validArgument(decoded)) return null
                decoded
            }
        if (argument != null && name !in setOf("word", "figure", "proverb")) return null
        return when (name) {
            "", "home" -> HomeKey
            "words", "word-list" -> WordsKey
            "bookmarks" -> BookmarksKey
            "word" -> argument?.let { DetailKey(it) }
            "proverb" -> ProverbKey(argument)
            "figures" -> FigureKey
            "figure" -> argument?.let { FigureDetailKey(it) }
            "settings" -> SettingsKey
            "privacy", "privacy-policy" -> LegalDocumentKey("privacy")
            "terms", "terms-conditions" -> LegalDocumentKey("terms")
            "licenses", "open-source-licenses" -> LegalDocumentKey("licenses")
            "ai-settings" -> AiSettingsKey
            "update" -> AppUpdateKey
            else -> null
        }
    }

    fun sharedText(text: String): NavKey? {
        if (text.length > 4096 || text.any { it.isISOControl() && !it.isWhitespace() }) return null
        val word =
            text
                .trim()
                .split(Regex("\\s+"), limit = 2)
                .firstOrNull()
                ?.trim { !it.isLetterOrDigit() && it != '-' }
                ?.lowercase()
                ?.takeIf(::validArgument)
                ?: return null
        return DetailKey(word)
    }

    fun validWidgetWord(word: String?): String? = word?.trim()?.takeIf { it.length <= MAX_ARGUMENT_LENGTH && validArgument(it) }

    fun widgetLink(input: String): Pair<WidgetKind, String?>? {
        if (input.length > MAX_LINK_LENGTH || input.any(Char::isISOControl)) return null
        val pieces =
            if (input.startsWith("#/widget/")) {
                input.removePrefix("#/widget/").split('/')
            } else {
                val url = runCatching { Url(input) }.getOrNull() ?: return null
                if (url.protocol.name.lowercase() != "kbbi" || url.host != "widget" || !url.user.isNullOrEmpty() ||
                    !url.password.isNullOrEmpty() ||
                    url.parameters
                        .names()
                        .isNotEmpty()
                ) {
                    return null
                }
                url.encodedPath.removePrefix("/").split('/')
            }
        if (pieces.size !in 1..2 || pieces.any(String::isEmpty)) return null
        val kind =
            when (pieces[0]) {
                "word-of-day" -> WidgetKind.WORD_OF_DAY
                "saved-word" -> WidgetKind.SAVED_WORD
                "quick-search" -> WidgetKind.QUICK_SEARCH
                else -> return null
            }
        if (kind == WidgetKind.QUICK_SEARCH && pieces.size != 1) return null
        val word =
            pieces.getOrNull(1)?.let { encoded ->
                if (encoded.length > MAX_ARGUMENT_LENGTH * 3 ||
                    Regex("%(?![0-9a-fA-F]{2})").containsMatchIn(
                        encoded,
                    )
                ) {
                    return null
                }
                validWidgetWord(runCatching { encoded.decodeURLPart() }.getOrNull()) ?: return null
            }
        return kind to word
    }

    fun isAvailable(key: NavKey): Boolean =
        when (key) {
            HomeKey, WordsKey, BookmarksKey, FigureKey, SettingsKey, AiSettingsKey -> true
            is DetailKey -> validArgument(key.word)
            is FigureDetailKey -> validArgument(key.slug)
            is ProverbKey -> key.slug == null || validArgument(key.slug)
            is LegalDocumentKey -> key.document in setOf("privacy", "terms", "licenses")
            else -> false
        }

    fun path(key: NavKey): String =
        when (key) {
            HomeKey -> "/home"
            WordsKey -> "/words"
            BookmarksKey -> "/bookmarks"
            is DetailKey -> "/word/${key.word.encodeURLPathPart()}"
            FigureKey -> "/figures"
            is FigureDetailKey -> "/figure/${key.slug.encodeURLPathPart()}"
            is ProverbKey -> key.slug?.let { "/proverb/${it.encodeURLPathPart()}" } ?: "/proverb"
            SettingsKey -> "/settings"
            is LegalDocumentKey -> "/${key.document}"
            AiSettingsKey -> "/ai-settings"
            AppUpdateKey -> "/update"
            else -> "/home"
        }

    private fun validArgument(value: String): Boolean =
        value.isNotBlank() && value.length <= MAX_ARGUMENT_LENGTH && value.none { it.isISOControl() || it in "/\\?#" } && value != "." &&
            value != ".."
}
