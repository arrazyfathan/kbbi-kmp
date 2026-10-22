package com.arrazyfathan.kbbi.feature.appupdate.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdateRequirement
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.app_update_action_failed
import kbbi_kmp.shared.generated.resources.app_update_available
import kbbi_kmp.shared.generated.resources.app_update_dialog_required_title
import kbbi_kmp.shared.generated.resources.app_update_dialog_title
import kbbi_kmp.shared.generated.resources.app_update_later
import kbbi_kmp.shared.generated.resources.app_update_open_release
import kbbi_kmp.shared.generated.resources.app_update_release_notes
import kbbi_kmp.shared.generated.resources.app_update_reload
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppUpdatePrompt(
    update: AppUpdate,
    actionFailed: Boolean,
    onLaunch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val required = update.requirement == AppUpdateRequirement.REQUIRED
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { if (!required) onDismiss() },
        title = {
            Text(
                stringResource(
                    if (required) Res.string.app_update_dialog_required_title else Res.string.app_update_dialog_title,
                ),
            )
        },
        text = {
            androidx.compose.foundation.layout.Column {
                Text(stringResource(Res.string.app_update_available, update.latestVersion))
                if (!update.releaseNotes.isNullOrBlank()) {
                    Text(
                        stringResource(Res.string.app_update_release_notes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(update.releaseNotes)
                }
                if (actionFailed) Text(stringResource(Res.string.app_update_action_failed), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = onLaunch) {
                Text(stringResource(if (update.actionUrl == null) Res.string.app_update_reload else Res.string.app_update_open_release))
            }
        },
        dismissButton = {
            if (!required) TextButton(onClick = onDismiss) { Text(stringResource(Res.string.app_update_later)) }
        },
    )
}
