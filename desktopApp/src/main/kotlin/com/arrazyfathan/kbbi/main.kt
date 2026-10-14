package com.arrazyfathan.kbbi

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.di.initKoin
import com.arrazyfathan.kbbi.feature.reminders.DesktopReminderScheduler
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun main() {
    AppLogger.plantDebugTree()
    val reminderScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    initKoin()
    loadKoinModules(
        module {
            single<ReminderScheduler> {
                DesktopReminderScheduler(
                    get(),
                    get(),
                    reminderScope,
                )
            }
        },
    )

    application {
        Window(
            onCloseRequest = {
                (GlobalContext.get().get<ReminderScheduler>() as? DesktopReminderScheduler)?.close()
                reminderScope.cancel()
                exitApplication()
            },
            title = "KBBI",
        ) {
            App()
        }
    }
}
