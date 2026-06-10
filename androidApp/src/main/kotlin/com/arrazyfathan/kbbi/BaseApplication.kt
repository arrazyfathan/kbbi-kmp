package com.arrazyfathan.kbbi

import android.app.Application
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.di.initKoin
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        AppLogger.plantDebugTree()

        initKoin {
            androidContext(this@BaseApplication)
            androidLogger()
        }
    }
}
