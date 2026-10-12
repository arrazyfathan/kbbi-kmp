package com.arrazyfathan.kbbi.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import kbbi_kmp.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LanguageSection(
    selectedLanguage: AppLanguage,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.language_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.background,
            )
            Row(
                modifier =
                    Modifier
                        .clickable(onClick = onClick)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LanguageBadge(language = selectedLanguage)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.app_language_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextH1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(selectedLanguage.labelResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextP,
                    )
                }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguagePickerBottomSheet(
    sheetState: SheetState,
    selectedLanguage: AppLanguage,
    isSelectionInProgress: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup().padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.choose_language_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            AppLanguage.entries.forEach { language ->
                LanguageOptionRow(
                    language = language,
                    selected = language == selectedLanguage,
                    enabled = !isSelectionInProgress,
                    onClick = { onLanguageSelected(language) },
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    language: AppLanguage,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageBadge(language = language)
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(language.labelResId),
            style = MaterialTheme.typography.bodyLarge,
            color = TextH1,
            modifier = Modifier.weight(1f),
        )
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = null,
        )
    }
}

@Composable
private fun LanguageBadge(language: AppLanguage) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .background(
                    brush =
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                        ),
                    CircleShape,
                ).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = language.badgeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private val AppLanguage.labelResId: StringResource
    get() =
        when (this) {
            AppLanguage.INDONESIAN -> Res.string.language_indonesian
            AppLanguage.ENGLISH -> Res.string.language_english
        }

private val AppLanguage.badgeLabel: String
    get() =
        when (this) {
            AppLanguage.INDONESIAN -> "ID"
            AppLanguage.ENGLISH -> "EN"
        }

