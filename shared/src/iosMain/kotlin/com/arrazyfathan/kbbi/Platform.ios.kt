package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.data.visitor.IosVisitorIdStorage
import com.arrazyfathan.kbbi.core.domain.visitor.StoredVisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.IosAlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.IosHaptics
import com.arrazyfathan.kbbi.core.platform.IosLocaleProvider
import com.arrazyfathan.kbbi.core.platform.IosTextShare
import com.arrazyfathan.kbbi.core.platform.IosUrlOpener
import com.arrazyfathan.kbbi.core.platform.IosWidgetSnapshotPublisher
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import com.arrazyfathan.kbbi.core.platform.PlatformHaptics
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import com.arrazyfathan.kbbi.core.platform.TextShare
import com.arrazyfathan.kbbi.core.platform.UnsupportedNotificationPermission
import com.arrazyfathan.kbbi.core.platform.UnsupportedSpeechInput
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.core.platform.WidgetSnapshotPublisher
import com.arrazyfathan.kbbi.feature.reminders.IosReminderScheduler
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.navigation.IosIncomingLaunchRequests
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val supportsNativeToast: Boolean = true
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
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
        single<PlatformHaptics> { IosHaptics() }
        single<SpeechInput> { UnsupportedSpeechInput }
        single<NotificationPermission> { UnsupportedNotificationPermission }
        single<AlternateAppIcon> { IosAlternateAppIcon() }
        single<IncomingLaunchRequests> { IosIncomingLaunchRequests }
        single<WidgetSnapshotPublisher> { IosWidgetSnapshotPublisher }
        single<ReminderScheduler> { IosReminderScheduler(get(), get()) }
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) {
    // No-op on iOS
}

private var activeToast: platform.UIKit.UIView? = null

actual fun showToast(message: String) {
    platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
        val window = UIApplication.sharedApplication.keyWindow ?: return@dispatch_async
        activeToast?.removeFromSuperview()
        val bubble =
            platform.UIKit.UIView().apply {
                translatesAutoresizingMaskIntoConstraints = false
                backgroundColor =
                    platform.UIKit.UIColor.blackColor
                        .colorWithAlphaComponent(0.85)
                layer.cornerRadius = 12.0
                userInteractionEnabled = false
                alpha = 0.0
            }
        val label =
            platform.UIKit.UILabel().apply {
                translatesAutoresizingMaskIntoConstraints = false
                text = message
                textColor = platform.UIKit.UIColor.whiteColor
                font = platform.UIKit.UIFont.systemFontOfSize(14.0)
                numberOfLines = 0
                textAlignment = platform.UIKit.NSTextAlignmentCenter
            }
        bubble.addSubview(label)
        window.addSubview(bubble)
        platform.UIKit.NSLayoutConstraint.activateConstraints(
            listOf(
                bubble.centerXAnchor.constraintEqualToAnchor(window.centerXAnchor),
                bubble.bottomAnchor.constraintEqualToAnchor(
                    window.safeAreaLayoutGuide.bottomAnchor,
                    constant = -24.0,
                ),
                bubble.leadingAnchor.constraintGreaterThanOrEqualToAnchor(
                    window.leadingAnchor,
                    constant = 20.0,
                ),
                bubble.trailingAnchor.constraintLessThanOrEqualToAnchor(
                    window.trailingAnchor,
                    constant = -20.0,
                ),
                label.leadingAnchor.constraintEqualToAnchor(bubble.leadingAnchor, constant = 16.0),
                label.trailingAnchor.constraintEqualToAnchor(
                    bubble.trailingAnchor,
                    constant = -16.0,
                ),
                label.topAnchor.constraintEqualToAnchor(bubble.topAnchor, constant = 12.0),
                label.bottomAnchor.constraintEqualToAnchor(bubble.bottomAnchor, constant = -12.0),
            ),
        )
        activeToast = bubble
        platform.UIKit.UIView.animateWithDuration(0.15, animations = { bubble.alpha = 1.0 })
        platform.darwin.dispatch_after(
            platform.darwin.dispatch_time(platform.darwin.DISPATCH_TIME_NOW, 2_000_000_000L),
            platform.darwin.dispatch_get_main_queue(),
        ) {
            platform.UIKit.UIView.animateWithDuration(
                0.2,
                animations = { bubble.alpha = 0.0 },
                completion = {
                    bubble.removeFromSuperview()
                    if (activeToast === bubble) activeToast = null
                },
            )
        }
    }
}

actual fun getAppVersionName(): String = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: ""
