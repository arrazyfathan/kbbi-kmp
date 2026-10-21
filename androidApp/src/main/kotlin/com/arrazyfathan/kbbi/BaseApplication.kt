package com.arrazyfathan.kbbi

import android.app.Application
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.NotificationPermission
import com.arrazyfathan.kbbi.core.platform.WidgetSnapshotPublisher
import com.arrazyfathan.kbbi.di.initKoin
import com.arrazyfathan.kbbi.feature.campaigns.domain.CampaignSubscriptionReconciler
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.notifications.AndroidCampaignPermission
import com.arrazyfathan.kbbi.notifications.AndroidCampaignSubscriptionReconciler
import com.arrazyfathan.kbbi.notifications.AndroidCampaignTopics
import com.arrazyfathan.kbbi.notifications.AndroidIncomingLaunchRequests
import com.arrazyfathan.kbbi.notifications.AndroidReminderScheduler
import com.arrazyfathan.kbbi.widgets.AndroidWidgetSnapshotPublisher
import com.arrazyfathan.kbbi.widgets.WidgetRefreshScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        AppLogger.plantDebugTree()

        initKoin {
            androidContext(this@BaseApplication)
            androidLogger()
        }
        loadKoinModules(
            module {
                single<ReminderScheduler> { AndroidReminderScheduler(this@BaseApplication, get()) }
                single<IncomingLaunchRequests> { AndroidIncomingLaunchRequests }
                single<WidgetSnapshotPublisher> { AndroidWidgetSnapshotPublisher(this@BaseApplication) }
                single<CampaignSubscriptionReconciler> {
                    AndroidCampaignSubscriptionReconciler(this@BaseApplication)
                }
                single<NotificationPermission> { AndroidCampaignPermission() }
            },
        )
        WidgetRefreshScheduler.reconcile(this)
        AndroidCampaignTopics.enqueue(this)
    }
}
