package com.arrazyfathan.kbbi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.platform.DesktopWindowIcon
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequest
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
import java.awt.Desktop

fun main(args: Array<String>) {
    AppLogger.plantDebugTree()
    val launchBroker = DesktopLaunchBroker.startOrForward(args.toList()) ?: return
    DesktopLaunchRequests.parse(args.toList())?.let(DesktopLaunchRequests::publish)
    if (Desktop.isDesktopSupported() &&
        Desktop
            .getDesktop()
            .isSupported(Desktop.Action.APP_OPEN_URI)
    ) {
        Desktop.getDesktop().setOpenURIHandler { event ->
            DesktopLaunchRequests.publish(
                IncomingLaunchRequest(
                    uri = event.uri.toString(),
                    requestId = "mac-${System.nanoTime()}",
                ),
            )
        }
    }
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
            state = rememberWindowState(size = DpSize(450.dp, 800.dp)),
            resizable = false,
            onCloseRequest = {
                launchBroker.close()
                (GlobalContext.get().get<ReminderScheduler>() as? DesktopReminderScheduler)?.close()
                reminderScope.cancel()
                exitApplication()
            },
            title = "KBBI",
            icon = DesktopWindowIcon(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                App()
            }
        }
    }
}
