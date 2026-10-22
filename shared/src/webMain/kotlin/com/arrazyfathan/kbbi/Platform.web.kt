package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.data.visitor.WebVisitorIdStorage
import com.arrazyfathan.kbbi.core.domain.visitor.StoredVisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdProvider
import com.arrazyfathan.kbbi.core.domain.visitor.VisitorIdStorage
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.BrowserAlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.BrowserLocaleProvider
import com.arrazyfathan.kbbi.core.platform.BrowserUrlOpener
import com.arrazyfathan.kbbi.core.platform.EmptyIncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.LocaleProvider
import com.arrazyfathan.kbbi.core.platform.NoOpWidgetSnapshotPublisher
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import com.arrazyfathan.kbbi.core.platform.PlatformHaptics
import com.arrazyfathan.kbbi.core.platform.SpeechInput
import com.arrazyfathan.kbbi.core.platform.TextShare
import com.arrazyfathan.kbbi.core.platform.UnsupportedNotificationPermission
import com.arrazyfathan.kbbi.core.platform.UnsupportedPlatformHaptics
import com.arrazyfathan.kbbi.core.platform.UnsupportedSpeechInput
import com.arrazyfathan.kbbi.core.platform.UnsupportedTextShare
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.core.platform.WidgetSnapshotPublisher
import com.arrazyfathan.kbbi.feature.reminders.BrowserReminderScheduler
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignSubscriptionReconciler
import com.arrazyfathan.kbbi.feature.campaigns.domain.NoOpCampaignSubscriptionReconciler
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdatePlatformMetadata
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateTarget
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateLauncher
import com.arrazyfathan.kbbi.shared.BuildKonfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.module.Module
import org.koin.dsl.module
import web.window.window

private class WebPlatform : Platform {
    override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebPlatform()

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { Js.create() }
        single<VisitorIdStorage> { WebVisitorIdStorage() }
        single<VisitorIdProvider> { StoredVisitorIdProvider(get()) }
        single<TextShare> { UnsupportedTextShare }
        single<UrlOpener> { BrowserUrlOpener() }
        single<LocaleProvider> { BrowserLocaleProvider() }
        single<PlatformHaptics> { UnsupportedPlatformHaptics }
        single<SpeechInput> { UnsupportedSpeechInput }
        single<NotificationPermission> { UnsupportedNotificationPermission }
        single<CampaignSubscriptionReconciler> { NoOpCampaignSubscriptionReconciler }
        single<AlternateAppIcon> { BrowserAlternateAppIcon() }
        single<AppUpdateLauncher> {
            AppUpdateLauncher { update ->
                if (update.actionUrl == null) {
                    window.location.reload()
                    CapabilityResult.Success(Unit)
                } else {
                    BrowserUrlOpener().open(update.actionUrl)
                }
            }
        }
        single<IncomingLaunchRequests> { EmptyIncomingLaunchRequests }
        single<ReminderScheduler> { BrowserReminderScheduler(get(), get()) }
        single<WidgetSnapshotPublisher> { NoOpWidgetSnapshotPublisher }
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) = Unit

actual fun showToast(message: String) {
    println(message)
}

actual fun getAppVersionName(): String = BuildKonfig.APP_VERSION

actual fun getAppUpdatePlatformMetadata(): AppUpdatePlatformMetadata =
    AppUpdatePlatformMetadata(
        target = AppUpdateTarget.WEB,
        currentVersion = getAppVersionName(),
        enabled = getAppVersionName().isNotBlank() && BuildKonfig.APP_ENVIRONMENT == "production",
    )
