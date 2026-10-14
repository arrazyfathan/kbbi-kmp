package com.arrazyfathan.kbbi

import android.app.Application
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
import com.arrazyfathan.kbbi.di.initKoin
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import com.arrazyfathan.kbbi.notifications.AndroidIncomingLaunchRequests
import com.arrazyfathan.kbbi.notifications.AndroidReminderScheduler
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
            },
        )
    }
}
