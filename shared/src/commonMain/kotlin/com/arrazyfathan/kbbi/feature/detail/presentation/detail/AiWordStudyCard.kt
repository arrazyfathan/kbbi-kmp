package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.ai_settings_backend_title
import kbbi_kmp.shared.generated.resources.ai_settings_custom_title
import kbbi_kmp.shared.generated.resources.ai_word_study_attribution_backend
import kbbi_kmp.shared.generated.resources.ai_word_study_attribution_custom
import kbbi_kmp.shared.generated.resources.ai_word_study_configure
import kbbi_kmp.shared.generated.resources.ai_word_study_custom_incomplete
import kbbi_kmp.shared.generated.resources.ai_word_study_disclaimer
import kbbi_kmp.shared.generated.resources.ai_word_study_disclaimer_ai_source
import kbbi_kmp.shared.generated.resources.ai_word_study_examples
import kbbi_kmp.shared.generated.resources.ai_word_study_generate
import kbbi_kmp.shared.generated.resources.ai_word_study_intro
import kbbi_kmp.shared.generated.resources.ai_word_study_loading
import kbbi_kmp.shared.generated.resources.ai_word_study_regenerate
import kbbi_kmp.shared.generated.resources.ai_word_study_related_words
import kbbi_kmp.shared.generated.resources.ai_word_study_title
import kbbi_kmp.shared.generated.resources.ai_word_study_usage_notes
import kbbi_kmp.shared.generated.resources.ic_auto_awesome
import kbbi_kmp.shared.generated.resources.ic_info
import kbbi_kmp.shared.generated.resources.retry
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AiWordStudyCard(
    state: DetailState,
    aiGenerated: Boolean,
    onAction: (DetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AiWordStudyHeader(state.aiConfiguration.providerMode)
            when {
                state.isWordStudyLoading -> {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.ai_word_study_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextH1,
                        )
                    }
                }

                state.wordStudy != null -> {
                    WordStudyResultContent(state.wordStudy)
                    OutlinedButton(
                        onClick = { onAction(DetailAction.OnGenerateWordStudy) },
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.ai_word_study_regenerate))
                    }
                }

                state.wordStudyErrorResId != null -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            text = stringResource(state.wordStudyErrorResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAction(DetailAction.OnRetryWordStudy) },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(Res.string.retry))
                        }
                        if (state.aiConfiguration.providerMode == AiProviderMode.CUSTOM) {
                            OutlinedButton(
                                onClick = { onAction(DetailAction.OnConfigureAi) },
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(Res.string.ai_word_study_configure))
                            }
                        }
                    }
                }

                else -> {
                    val customIncomplete =
                        state.aiConfiguration.providerMode == AiProviderMode.CUSTOM && !state.aiConfiguration.isCustomConfigurationComplete
                    Text(
                        text =
                            stringResource(
                                if (customIncomplete) {
                                    Res.string.ai_word_study_custom_incomplete
                                } else {
                                    Res.string.ai_word_study_intro
                                },
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextP,
                    )
                    Button(
                        onClick = {
                            onAction(
                                if (customIncomplete) DetailAction.OnConfigureAi else DetailAction.OnGenerateWordStudy,
                            )
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                if (customIncomplete) {
                                    Res.string.ai_word_study_configure
                                } else {
                                    Res.string.ai_word_study_generate
                                },
                            ),
                        )
                    }
                }
            }
            AiDisclaimer(aiGenerated)
        }
    }
}

@Composable
private fun AiWordStudyHeader(providerMode: AiProviderMode) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_auto_awesome),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.ai_word_study_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextH1,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    stringResource(
                        if (providerMode == AiProviderMode.BACKEND) {
                            Res.string.ai_settings_backend_title
                        } else {
                            Res.string.ai_settings_custom_title
                        },
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun WordStudyResultContent(result: WordStudyModel) {
    Text(
        text = result.explanation,
        style = MaterialTheme.typography.bodyLarge,
        color = TextH1,
        lineHeight = 24.sp,
    )
    WordStudyNumberedList(stringResource(Res.string.ai_word_study_examples), result.examples)
    WordStudyNumberedList(stringResource(Res.string.ai_word_study_usage_notes), result.usageNotes)
    RelatedWords(result.relatedWords)
    Text(
        text =
            if (result.providerMode == AiProviderMode.BACKEND) {
                stringResource(Res.string.ai_word_study_attribution_backend)
            } else {
                stringResource(
                    Res.string.ai_word_study_attribution_custom,
                    result.provider,
                    result.model,
                )
            },
        style = MaterialTheme.typography.labelSmall,
        color = TextP,
    )
}

@Composable
private fun WordStudyNumberedList(
    title: String,
    values: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextH1,
            fontWeight = FontWeight.SemiBold,
        )
        values.forEachIndexed { index, value ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 1.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextP,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedWords(values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(Res.string.ai_word_study_related_words),
            style = MaterialTheme.typography.titleSmall,
            color = TextH1,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { word ->
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AiDisclaimer(aiGenerated: Boolean) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_info),
            contentDescription = null,
            tint = TextP,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text =
                stringResource(
                    if (aiGenerated) Res.string.ai_word_study_disclaimer_ai_source else Res.string.ai_word_study_disclaimer,
                ),
            style = MaterialTheme.typography.bodySmall,
            color = TextP,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AiWordStudyResultPreview() {
    com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme {
        AiWordStudyCard(
            state =
                DetailState(
                    wordStudy =
                        WordStudyModel(
                            explanation = "Bahasa adalah sistem lambang bunyi yang digunakan untuk berkomunikasi.",
                            examples =
                                listOf(
                                    "Kami belajar bahasa Indonesia.",
                                    "Bahasa menyatukan masyarakat.",
                                ),
                            usageNotes =
                                listOf(
                                    "Digunakan sebagai kata benda.",
                                    "Dapat merujuk pada cara mengungkapkan pikiran.",
                                ),
                            relatedWords = listOf("kata", "tutur", "ucap"),
                            providerMode = AiProviderMode.BACKEND,
                            provider = "KBBI",
                            model = "",
                        ),
                ),
            aiGenerated = false,
            onAction = {},
        )
    }
}
