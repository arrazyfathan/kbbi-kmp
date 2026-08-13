package com.arrazyfathan.kbbi

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.data.visitor.IosVisitorIdStorage
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
