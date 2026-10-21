package com.arrazyfathan.kbbi.feature.campaigns.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.app_update_available
import kbbi_kmp.shared.generated.resources.app_update_details_fallback
import kbbi_kmp.shared.generated.resources.app_update_title
import kbbi_kmp.shared.generated.resources.app_update_up_to_date
import kbbi_kmp.shared.generated.resources.ic_arrow_back
import kbbi_kmp.shared.generated.resources.navigate_back
import kbbi_kmp.shared.generated.resources.update_open_release_page
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

data class AppUpdateCopy(val versionName: String, val title: String, val body: String)

expect fun readAppUpdateCopy(releaseId: String): AppUpdateCopy?
expect fun configuredAppReleaseUrl(): String?

@Composable
fun UpdateAnnouncementRoute(releaseId: String, onBack: () -> Unit) {
    val copy = remember(releaseId) { readAppUpdateCopy(releaseId) }
    val urlOpener: UrlOpener = koinInject()
    val releaseUrl = remember { configuredAppReleaseUrl()?.takeIf { it.startsWith("https://") } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_update_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                copy?.title ?: stringResource(Res.string.app_update_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                copy?.versionName?.let { stringResource(Res.string.app_update_available, it) }
                    ?: stringResource(Res.string.app_update_up_to_date),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(copy?.body ?: stringResource(Res.string.app_update_details_fallback))
            if (releaseUrl != null) {
                Button(onClick = { urlOpener.open(releaseUrl) }) {
                    Text(stringResource(Res.string.update_open_release_page))
                }
            }
        }
    }
}
