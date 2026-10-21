package com.arrazyfathan.kbbi.feature.campaigns.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class CampaignPayloadParserTest {
    private val now = Instant.parse("2026-10-20T08:00:00Z")

    @Test
    fun editorialPayloadAcceptsSupportedWordAndProverbDestinations() {
        val word = CampaignPayloadParser.parseEditorial(editorial("word/kamus"), now)
        assertEquals(CampaignDestination.Word("kamus"), word?.destination)
        val proverb = CampaignPayloadParser.parseEditorial(editorial("proverb/Air_besar"), now)
        assertEquals(CampaignDestination.Proverb("Air_besar"), proverb?.destination)
    }

    @Test
    fun editorialPayloadRejectsUnsupportedTopicsMalformedDestinationsAndExpiredMessages() {
        assertNull(CampaignPayloadParser.parseEditorial(editorial("bookmark/word"), now))
        assertNull(CampaignPayloadParser.parseEditorial(editorial("word/a/b"), now))
        assertNull(CampaignPayloadParser.parseEditorial(editorial("word/kamus", expires = "2026-10-20T07:59:59Z"), now))
    }

    @Test
    fun updatePayloadRequiresProductionIosVersionAndFutureExpiry() {
        val valid = update()
        assertTrue(CampaignPayloadParser.parseUpdate(valid, now, CampaignEnvironment.PRODUCTION, CampaignPlatform.IOS) != null)
        assertNull(CampaignPayloadParser.parseUpdate(valid, now, CampaignEnvironment.DEVELOPMENT, CampaignPlatform.IOS))
        assertNull(CampaignPayloadParser.parseUpdate(valid + ("platform" to "android"), now, CampaignEnvironment.PRODUCTION, CampaignPlatform.IOS))
        assertNull(CampaignPayloadParser.parseUpdate(update(expires = "2026-10-20T08:00:00Z"), now, CampaignEnvironment.PRODUCTION, CampaignPlatform.IOS))
        assertNull(CampaignPayloadParser.parseUpdate(update(version = "1.2-beta"), now, CampaignEnvironment.PRODUCTION, CampaignPlatform.IOS))
    }

    @Test
    fun versionsCompareNumericallyAndRejectMalformedInput() {
        assertTrue(CampaignVersionComparator.isNewer("1.10.0", "1.9.9"))
        assertFalse(CampaignVersionComparator.isNewer("2.0", "2.0.0"))
        assertFalse(CampaignVersionComparator.isNewer("1.x", "1.0"))
    }

    private fun editorial(destination: String, expires: String = "2026-10-20T08:01:00Z") = mapOf(
        "schema_version" to "1", "delivery_id" to "delivery-1", "campaign_id" to "campaign-1",
        "topic" to "word_of_day", "title" to "Word today", "body" to "Open the dictionary",
        "destination" to destination, "expires_at" to expires,
    )

    private fun update(version: String = "2.3.1", expires: String = "2026-10-20T08:01:00Z") = mapOf(
        "type" to "app_update", "schema_version" to "1", "flavor" to "production", "platform" to "ios",
        "version_name" to version, "version_code" to "20301", "release_id" to "ios-2-3-1",
        "title" to "Update available", "body" to "See what is new", "expires_at" to expires,
    )
}
