package com.arrazyfathan.kbbi.core.platform

import com.arrazyfathan.kbbi.platformModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.koin.dsl.koinApplication

class PlatformCapabilitiesTest {
    @Test
    fun deniedAndUnavailableAreDistinctContractOutcomes() {
        val deniedShare = TextShare { CapabilityResult.Denied }
        val denied: CapabilityResult<Unit> = CapabilityResult.Denied
        val unavailable: CapabilityResult<Unit> = CapabilityResult.Unavailable

        assertEquals(denied, deniedShare.share("text"))
        assertEquals(CapabilityResult.Unavailable, UnsupportedTextShare.share("text"))
        assertEquals(false, denied == unavailable)
    }

    @Test
    fun unsupportedHapticsAndAlternateIconsReportUnavailable() {
        assertEquals(CapabilityResult.Unavailable, UnsupportedHaptics.perform(HapticFeedback.TAP))
        assertEquals(CapabilityResult.Unavailable, UnsupportedAlternateAppIcon.setIcon(AppIconVariant.DEFAULT))
    }

    @Test
    fun activePlatformModuleResolvesEveryCapability() {
        val app = koinApplication(createEagerInstances = false) { modules(platformModule) }

        try {
            val koin = app.koin
            assertNotNull(koin.get<TextShare>())
            assertNotNull(koin.get<UrlOpener>())
            assertNotNull(koin.get<LocaleProvider>())
            assertNotNull(koin.get<Haptics>())
            assertNotNull(koin.get<SpeechInput>())
            assertNotNull(koin.get<NotificationPermission>())
            assertNotNull(koin.get<AlternateAppIcon>())
            assertNotNull(koin.get<IncomingLaunchRequests>())
        } finally {
            app.close()
        }
    }
}
