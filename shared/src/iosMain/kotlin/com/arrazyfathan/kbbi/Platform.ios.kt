package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.data.visitor.IosVisitorIdStorage
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.EmptyIncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.Haptics
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.IosHaptics
import com.arrazyfathan.kbbi.core.platform.IosLocaleProvider
import com.arrazyfathan.kbbi.core.platform.IosTextShare
import com.arrazyfathan.kbbi.core.platform.IosUrlOpener
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import com.arrazyfathan.kbbi.core.platform.TextShare
import com.arrazyfathan.kbbi.core.platform.UnsupportedAlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.UnsupportedNotificationPermission
import com.arrazyfathan.kbbi.core.platform.UnsupportedSpeechInput
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.core.domain.visitor.StoredVisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { Darwin.create() }
        single<VisitorIdStorage> { IosVisitorIdStorage() }
        single<VisitorIdProvider> { StoredVisitorIdProvider(get()) }
        single<TextShare> { IosTextShare() }
        single<UrlOpener> { IosUrlOpener() }
        single<LocaleProvider> { IosLocaleProvider() }
        single<Haptics> { IosHaptics() }
        single<SpeechInput> { UnsupportedSpeechInput }
        single<NotificationPermission> { UnsupportedNotificationPermission }
        single<AlternateAppIcon> { UnsupportedAlternateAppIcon }
        single<IncomingLaunchRequests> { EmptyIncomingLaunchRequests }
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) {
    // No-op on iOS
}

actual fun showToast(message: String) {
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootViewController = keyWindow?.rootViewController
    if (rootViewController != null) {
        val alert =
            UIAlertController.alertControllerWithTitle(
                title = null,
                message = message,
                preferredStyle = UIAlertControllerStyleAlert,
            )
        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = "OK",
                style = UIAlertActionStyleDefault,
                handler = null,
            ),
        )
        rootViewController.presentViewController(alert, animated = true, completion = null)
    }
}

actual fun getAppVersionName(): String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
        ?: ""
