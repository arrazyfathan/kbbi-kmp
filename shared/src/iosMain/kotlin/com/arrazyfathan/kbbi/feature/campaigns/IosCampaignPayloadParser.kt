package com.arrazyfathan.kbbi.feature.campaigns

import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignDestination
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignEnvironment
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignMessage
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignPayloadParser
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignPlatform
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignVersionComparator
import io.ktor.http.encodeURLPathPart
import platform.Foundation.NSBundle
import kotlin.time.Instant

data class IosParsedCampaignPush(
    val deliveryId: String,
    val topic: String,
    val title: String,
    val body: String,
    val route: String,
    val versionName: String?,
    val releaseId: String?,
)

object IosCampaignPayloadParser {
    fun parse(data: Map<String, String>, nowEpochMillis: Double): IosParsedCampaignPush? {
        val environment =
            if (NSBundle.mainBundle.objectForInfoDictionaryKey("KBBIEnvironment") as? String == "production") {
                CampaignEnvironment.PRODUCTION
            } else {
                CampaignEnvironment.DEVELOPMENT
            }
        if (data["environment"] != environment.wireValue || data["platform"] != CampaignPlatform.IOS.wireValue) return null
        if (data["type"] == "app_update" &&
            (data["title"].isNullOrBlank() || data["title"]!!.length > 100 || data["body"].isNullOrBlank() || data["body"]!!.length > 500)
        ) return null
        return when (
            val message = CampaignPayloadParser.parse(
                data,
                Instant.fromEpochMilliseconds(nowEpochMillis.toLong()),
                environment,
                CampaignPlatform.IOS,
            ) ?: return null
        ) {
            is CampaignMessage.Editorial -> {
                val route = when (val destination = message.value.destination) {
                    is CampaignDestination.Word -> "kbbi://word/${destination.term.encodeURLPathPart()}"
                    is CampaignDestination.Proverb -> "kbbi://proverb/${destination.slug.encodeURLPathPart()}"
                }
                IosParsedCampaignPush(
                    message.value.deliveryId,
                    message.value.topic.wireValue,
                    message.value.title,
                    message.value.body,
                    route,
                    null,
                    null,
                )
            }

            is CampaignMessage.AppUpdate -> {
                val currentVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: return null
                val currentCode = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toLongOrNull() ?: return null
                if (!CampaignVersionComparator.isNewer(message.value.versionName, currentVersion) || message.value.versionCode <= currentCode) return null
                IosParsedCampaignPush(
                    data["delivery_id"]?.takeIf { it.length <= 128 } ?: "update-${message.value.releaseId}",
                    "app_updates",
                    message.value.title,
                    message.value.body,
                    "kbbi://update/${message.value.releaseId.encodeURLPathPart()}",
                    message.value.versionName,
                    message.value.releaseId,
                )
            }
        }
    }
}
