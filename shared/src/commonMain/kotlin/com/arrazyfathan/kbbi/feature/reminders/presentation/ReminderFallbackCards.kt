package com.arrazyfathan.kbbi.feature.reminders.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arrazyfathan.kbbi.feature.reminders.domain.ReminderContent
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.reminder_fallback_dismiss
import kbbi_kmp.shared.generated.resources.reminder_fallback_open
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderFallbackCards(
    reminders: List<ReminderContent>,
    modifier: Modifier = Modifier,
    onOpen: (ReminderContent) -> Unit,
    onDismiss: (ReminderContent) -> Unit,
) {
    if (reminders.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        reminders.forEach { reminder ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                    Text(reminder.body, style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onDismiss(reminder) }) {
                            Text(stringResource(Res.string.reminder_fallback_dismiss))
                        }
                        TextButton(onClick = { onOpen(reminder) }) {
                            Text(stringResource(Res.string.reminder_fallback_open))
                        }
                    }
                }
            }
        }
    }
}
