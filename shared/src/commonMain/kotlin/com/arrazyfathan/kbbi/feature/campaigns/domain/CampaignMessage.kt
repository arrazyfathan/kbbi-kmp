package com.arrazyfathan.kbbi.feature.campaigns.domain

import kotlin.time.Instant

enum class CampaignTopic(
    val wireValue: String,
) {
    WORD_OF_DAY("word_of_day"),
    TRENDING_WORDS("trending_words"),
    PROVERBS("proverbs"),
    APP_UPDATES("app_updates"),
}

enum class CampaignEnvironment(
    val wireValue: String,
) {
    PRODUCTION("production"),
    DEVELOPMENT("development"),
}

enum class CampaignPlatform(
    val wireValue: String,
) {
    ANDROID("android"),
    IOS("ios"),
    DESKTOP("desktop"),
    WEB_JS("web_js"),
    WEB_WASM("web_wasm"),
}

sealed interface CampaignDestination {
    data class Word(val term: String) : CampaignDestination

    data class Proverb(val slug: String) : CampaignDestination
}

data class EditorialCampaign(
    val deliveryId: String,
    val campaignId: String,
    val topic: CampaignTopic,
    val title: String,
    val body: String,
    val destination: CampaignDestination,
    val expiresAt: Instant,
)

data class AppUpdateAnnouncement(
    val versionName: String,
    val versionCode: Long,
    val releaseId: String,
    val title: String,
    val body: String,
    val platform: CampaignPlatform,
    val expiresAt: Instant,
)

sealed interface CampaignMessage {
    data class Editorial(val value: EditorialCampaign) : CampaignMessage

    data class AppUpdate(val value: AppUpdateAnnouncement) : CampaignMessage
}

/** Validates untrusted FCM/Web Push data before it reaches storage, presentation, or navigation. */
object CampaignPayloadParser {
    private const val MAX_ID_LENGTH = 128
    private const val MAX_DESTINATION_LENGTH = 256
    private val versionPattern = Regex("^v?\\d+(\\.\\d+){1,3}$")
    private val allowedEditorialTopics =
        setOf(CampaignTopic.WORD_OF_DAY, CampaignTopic.TRENDING_WORDS, CampaignTopic.PROVERBS)

    fun parse(
        data: Map<String, String>,
        now: Instant,
        environment: CampaignEnvironment,
        platform: CampaignPlatform,
    ): CampaignMessage? =
        if (data["type"] == "app_update") {
            parseUpdate(data, now, environment, platform)?.let(CampaignMessage::AppUpdate)
        } else {
            parseEditorial(data, now)?.let(CampaignMessage::Editorial)
        }

    fun parseEditorial(
        data: Map<String, String>,
        now: Instant,
    ): EditorialCampaign? {
        if (data["schema_version"] != "1") return null
        val topic = allowedEditorialTopics.firstOrNull { it.wireValue == data["topic"] } ?: return null
        val deliveryId = data["delivery_id"]?.takeIf { it.isNotBlank() && it.length <= MAX_ID_LENGTH } ?: return null
        val campaignId = data["campaign_id"]?.takeIf { it.isNotBlank() && it.length <= MAX_ID_LENGTH } ?: return null
        val title = data["title"]?.takeIf { it.isNotBlank() && it.length <= 100 } ?: return null
        val body = data["body"]?.takeIf { it.isNotBlank() && it.length <= 500 } ?: return null
        val destination = parseDestination(data["destination"]) ?: return null
        val expiry = parseExpiry(data["expires_at"]) ?: return null
        if (expiry < now) return null
        return EditorialCampaign(deliveryId, campaignId, topic, title, body, destination, expiry)
    }

    fun parseUpdate(
        data: Map<String, String>,
        now: Instant,
        environment: CampaignEnvironment,
        platform: CampaignPlatform,
    ): AppUpdateAnnouncement? {
        if (data["schema_version"] != "1" || environment != CampaignEnvironment.PRODUCTION) return null
        if (data["flavor"] != CampaignEnvironment.PRODUCTION.wireValue) return null
        val target = CampaignPlatform.entries.firstOrNull { it.wireValue == data["platform"] } ?: return null
        if (target != platform) return null
        val version = data["version_name"]?.takeIf(versionPattern::matches)?.removePrefix("v") ?: return null
        val versionCode = data["version_code"]?.toLongOrNull()?.takeIf { it > 0 } ?: return null
        val releaseId = data["release_id"]?.takeIf { it.isNotBlank() && it.length <= 100 } ?: return null
        val title = data["title"]?.takeIf { it.length <= 100 }.orEmpty()
        val body = data["body"]?.takeIf { it.length <= 500 }.orEmpty()
        val expiry = parseExpiry(data["expires_at"]) ?: return null
        if (expiry <= now) return null
        return AppUpdateAnnouncement(version, versionCode, releaseId, title, body, target, expiry)
    }

    private fun parseDestination(value: String?): CampaignDestination? {
        if (value == null || value.length > MAX_DESTINATION_LENGTH) return null
        val separator = value.indexOf('/')
        if (separator <= 0 || separator == value.lastIndex) return null
        val kind = value.substring(0, separator)
        val destination = value.substring(separator + 1)
        if (destination.isBlank() || destination.any { it.isISOControl() || it in "/\\?#" }) return null
        if (destination == "." || destination == "..") return null
        return when (kind) {
            "word" -> CampaignDestination.Word(destination)
            "proverb" -> CampaignDestination.Proverb(destination)
            else -> null
        }
    }

    private fun parseExpiry(value: String?): Instant? =
        value?.let { raw -> runCatching { Instant.parse(raw) }.getOrNull() }
}

/** Compares the source's numeric version names without relying on platform-specific version APIs. */
object CampaignVersionComparator {
    fun isNewer(candidate: String, installed: String): Boolean {
        val left = normalize(candidate) ?: return false
        val right = normalize(installed) ?: return false
        val size = maxOf(left.size, right.size)
        for (index in 0 until size) {
            val comparison = (left.getOrElse(index) { 0 }).compareTo(right.getOrElse(index) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }

    fun normalize(version: String): List<Int>? {
        val value = version.removePrefix("v")
        if (!Regex("^v?\\d+(\\.\\d+){1,3}$").matches(version)) return null
        return value.split('.').map { it.toIntOrNull() ?: return null }
    }
}
