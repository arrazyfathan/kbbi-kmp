package com.arrazyfathan.kbbi.feature.appupdate.domain

object AppVersionComparator {
    fun normalize(version: String): String? {
        val normalized = version.trim().removePrefix("v").removePrefix("V").substringBefore('-').substringBefore('+')
        return normalized.takeIf { it.matches(Regex("\\d+(\\.\\d+)*")) }
    }

    fun compare(first: String, second: String): Int? {
        val firstParts = normalize(first)?.toIntParts() ?: return null
        val secondParts = normalize(second)?.toIntParts() ?: return null
        val length = maxOf(firstParts.size, secondParts.size)
        for (index in 0 until length) {
            val result = firstParts.getOrElse(index) { 0 }.compareTo(secondParts.getOrElse(index) { 0 })
            if (result != 0) return result
        }
        return 0
    }

    fun isNewer(latest: String, current: String): Boolean = compare(latest, current)?.let { it > 0 } == true

    fun isMajorUpdate(latest: String, current: String): Boolean {
        val latestMajor = normalize(latest)?.substringBefore('.')?.toIntOrNull() ?: return false
        val currentMajor = normalize(current)?.substringBefore('.')?.toIntOrNull() ?: return false
        return latestMajor > currentMajor
    }

    private fun String.toIntParts(): List<Int>? = split('.').map { it.toIntOrNull() ?: return null }
}
