package com.arrazyfathan.kbbi

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.arrazyfathan.kbbi.core.data.visitor.AndroidVisitorIdStorage
import com.arrazyfathan.kbbi.core.platform.AndroidHaptics
import com.arrazyfathan.kbbi.core.platform.AndroidLocaleProvider
import com.arrazyfathan.kbbi.core.platform.AndroidTextShare
import com.arrazyfathan.kbbi.core.platform.AndroidUrlOpener
import com.arrazyfathan.kbbi.core.platform.AlternateAppIcon
import com.arrazyfathan.kbbi.core.platform.EmptyIncomingLaunchRequests
import com.arrazyfathan.kbbi.core.platform.Haptics
import com.arrazyfathan.kbbi.core.platform.IncomingLaunchRequests
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
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { OkHttp.create() }
        single<VisitorIdStorage> { AndroidVisitorIdStorage() }
        single<VisitorIdProvider> { StoredVisitorIdProvider(get()) }
        single<TextShare> { AndroidTextShare() }
        single<UrlOpener> { AndroidUrlOpener() }
        single<LocaleProvider> { AndroidLocaleProvider() }
        single<Haptics> { AndroidHaptics() }
        single<SpeechInput> { UnsupportedSpeechInput }
        single<NotificationPermission> { UnsupportedNotificationPermission }
        single<AlternateAppIcon> { UnsupportedAlternateAppIcon }
        single<IncomingLaunchRequests> { EmptyIncomingLaunchRequests }
    }

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Composable
actual fun BindSystemBarColor(isDetailVisible: Boolean) {
    val context = LocalContext.current
    LaunchedEffect(isDetailVisible) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        val statusBarColor =
            if (isDetailVisible) {
                0xFFE8F0F1.toInt() // BlueBg
            } else {
                0xFF303E9F.toInt() // BluePrimary
            }
        // White
        activity.updateSystemBarStyle(
            statusBarColor,
            0xFFFFFFFF.toInt(),
        )
    }
}

actual fun showToast(message: String) {
    Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
}

actual fun getAppVersionName(): String =
    appContext.packageManager
        .getPackageInfo(appContext.packageName, 0)
        .versionName
        .orEmpty()
