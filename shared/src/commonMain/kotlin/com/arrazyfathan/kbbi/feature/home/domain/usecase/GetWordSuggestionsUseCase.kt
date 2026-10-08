package com.arrazyfathan.kbbi.feature.home.domain.usecase

import kotlin.math.abs

/** Produces prefix/substring matches followed by bounded edit-distance matches for “did you mean?”. */
class GetWordSuggestionsUseCase {
    operator fun invoke(
        query: String,
        words: List<String>,
        maxSuggestions: Int = DEFAULT_MAX_SUGGESTIONS,
    ): List<String> {
        if (maxSuggestions <= 0) return emptyList()
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.length < MIN_SUGGESTION_QUERY_LENGTH) return emptyList()

        val candidates =
            words.asSequence()
                .map { original -> WordCandidate(original, original.trim().lowercase()) }
                .filter { it.normalized.isNotBlank() }
                .distinctBy { it.normalized }
                .toList()

        val exact =
            (candidates.filter { it.normalized.startsWith(normalizedQuery) } +
                candidates.filter { normalizedQuery in it.normalized && !it.normalized.startsWith(normalizedQuery) })
                .distinctBy { it.normalized }
                .take(maxSuggestions)

        if (exact.size >= maxSuggestions || normalizedQuery.length < MIN_FUZZY_QUERY_LENGTH) {
            return exact.map { it.original }
        }

        val exactWords = exact.mapTo(mutableSetOf()) { it.normalized }
        val maxDistance = if (normalizedQuery.length <= 4) 1 else 2
        val fuzzy =
            candidates.asSequence()
                .filterNot { it.normalized in exactWords }
                .filter { abs(it.normalized.length - normalizedQuery.length) <= maxDistance }
                .mapNotNull { candidate ->
                    val distance = editDistance(normalizedQuery, candidate.normalized, maxDistance) ?: return@mapNotNull null
                    RankedCandidate(
                        candidate = candidate,
                        distance = distance,
                        sameFirstCharacter = candidate.normalized.firstOrNull() == normalizedQuery.firstOrNull(),
                        lengthDelta = abs(candidate.normalized.length - normalizedQuery.length),
                    )
                }
                .sortedWith(
                    compareBy<RankedCandidate> { it.distance }
                        .thenByDescending { it.sameFirstCharacter }
                        .thenBy { it.lengthDelta }
                        .thenBy { it.candidate.normalized },
                )
                .take(maxSuggestions - exact.size)
                .map { it.candidate }
                .toList()

        return (exact + fuzzy).map { it.original }
    }

    private fun editDistance(
        left: String,
        right: String,
        maxDistance: Int,
    ): Int? {
        if (abs(left.length - right.length) > maxDistance) return null
        var previousPrevious = IntArray(right.length + 1)
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)

        for (leftIndex in left.indices) {
            current[0] = leftIndex + 1
            for (rightIndex in right.indices) {
                val substitutionCost = if (left[leftIndex] == right[rightIndex]) 0 else 1
                var best =
                    minOf(
                        previous[rightIndex + 1] + 1,
                        current[rightIndex] + 1,
                        previous[rightIndex] + substitutionCost,
                    )
                if (
                    leftIndex > 0 && rightIndex > 0 &&
                    left[leftIndex] == right[rightIndex - 1] &&
                    left[leftIndex - 1] == right[rightIndex]
                ) {
                    best = minOf(best, previousPrevious[rightIndex - 1] + 1)
                }
                current[rightIndex + 1] = best
            }
            previousPrevious = previous.copyOf()
            val oldPrevious = previous
            previous = current
            current = oldPrevious
        }

        return previous[right.length].takeIf { it <= maxDistance }
    }

    private data class WordCandidate(
        val original: String,
        val normalized: String,
    )

    private data class RankedCandidate(
        val candidate: WordCandidate,
        val distance: Int,
        val sameFirstCharacter: Boolean,
        val lengthDelta: Int,
    )

    private companion object {
        const val MIN_SUGGESTION_QUERY_LENGTH = 2
        const val MIN_FUZZY_QUERY_LENGTH = 3
        const val DEFAULT_MAX_SUGGESTIONS = 8
    }
}
