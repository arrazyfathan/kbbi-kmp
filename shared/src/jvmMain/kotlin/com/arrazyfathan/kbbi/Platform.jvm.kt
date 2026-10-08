package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.data.visitor.JvmVisitorIdStorage
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.DesktopLocaleProvider
import com.arrazyfathan.kbbi.core.platform.DesktopUrlOpener
import com.arrazyfathan.kbbi.core.platform.EmptyIncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.Haptics
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import com.arrazyfathan.kbbi.core.platform.TextShare
import com.arrazyfathan.kbbi.core.platform.UnsupportedAlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.UnsupportedHaptics
import com.arrazyfathan.kbbi.core.platform.UnsupportedNotificationPermission
import com.arrazyfathan.kbbi.core.platform.UnsupportedSpeechInput
import com.arrazyfathan.kbbi.core.platform.UnsupportedTextShare
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.core.domain.visitor.StoredVisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.koin.core.module.Module
import org.koin.dsl.module

private class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM"
}

actual fun getPlatform(): Platform = DesktopPlatform()

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { CIO.create() }
        single<VisitorIdStorage> { JvmVisitorIdStorage() }
        single<VisitorIdProvider> { StoredVisitorIdProvider(get()) }
        single<TextShare> { UnsupportedTextShare }
        single<UrlOpener> { DesktopUrlOpener() }
        single<LocaleProvider> { DesktopLocaleProvider() }
        single<Haptics> { UnsupportedHaptics }
        single<SpeechInput> { UnsupportedSpeechInput }
        single<NotificationPermission> { UnsupportedNotificationPermission }
        single<AlternateAppIcon> { UnsupportedAlternateAppIcon }
        single<IncomingLaunchRequests> { EmptyIncomingLaunchRequests }
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) = Unit

actual fun showToast(message: String) {
    println(message)
}

actual fun getAppVersionName(): String = "1.0.0"
