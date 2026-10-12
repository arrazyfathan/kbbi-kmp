package com.arrazyfathan.kbbi.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.platform.CapabilityResult
import com.arrazyfathan.kbbi.core.platform.TextShare
import com.arrazyfathan.kbbi.core.platform.UrlOpener
import com.arrazyfathan.kbbi.core.presentation.designsystem.palette
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.KBBITimePickerBottomSheet
import com.arrazyfathan.kbbi.feature.settings.domain.AppLanguage
import com.arrazyfathan.kbbi.feature.settings.domain.AppIcon as AppIconModel
import com.arrazyfathan.kbbi.feature.settings.domain.ReminderType
import com.arrazyfathan.kbbi.getAppVersionName
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.navigate_back
import kbbi_kmp.shared.generated.resources.ic_arrow_back
import kbbi_kmp.shared.generated.resources.app_icon_preview_default
import kbbi_kmp.shared.generated.resources.app_icon_preview_royal_ocean
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_sunset
import kbbi_kmp.shared.generated.resources.app_icon_preview_golden_coral_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_deep_forest_energy
import kbbi_kmp.shared.generated.resources.app_icon_preview_neon_violet
import kbbi_kmp.shared.generated.resources.app_icon_preview_blaze_orange
import kbbi_kmp.shared.generated.resources.ic_vibration
import kbbi_kmp.shared.generated.resources.ic_bug
import kbbi_kmp.shared.generated.resources.ic_analytics
import kbbi_kmp.shared.generated.resources.ic_speed
import kbbi_kmp.shared.generated.resources.ic_update
import kbbi_kmp.shared.generated.resources.ic_info
import kbbi_kmp.shared.generated.resources.ic_privacy
import kbbi_kmp.shared.generated.resources.ic_document
import kbbi_kmp.shared.generated.resources.ic_code
import kbbi_kmp.shared.generated.resources.ic_history
import kbbi_kmp.shared.generated.resources.ic_proverb
import kbbi_kmp.shared.generated.resources.share
import kbbi_kmp.shared.generated.resources.word
import kbbi_kmp.shared.generated.resources.saved
import kbbi_kmp.shared.generated.resources.about_section_title
import kbbi_kmp.shared.generated.resources.app_icon_blaze_orange
import kbbi_kmp.shared.generated.resources.app_icon_default
import kbbi_kmp.shared.generated.resources.app_icon_deep_forest_energy
import kbbi_kmp.shared.generated.resources.app_icon_golden_coral_energy
import kbbi_kmp.shared.generated.resources.app_icon_golden_sunset
import kbbi_kmp.shared.generated.resources.app_icon_picker_description
import kbbi_kmp.shared.generated.resources.app_icon_picker_title
import kbbi_kmp.shared.generated.resources.app_icon_neon_violet
import kbbi_kmp.shared.generated.resources.app_icon_royal_ocean
import kbbi_kmp.shared.generated.resources.app_version_title
import kbbi_kmp.shared.generated.resources.app_update_title
import kbbi_kmp.shared.generated.resources.app_update_up_to_date
import kbbi_kmp.shared.generated.resources.appearance_section_title
import kbbi_kmp.shared.generated.resources.cancel
import kbbi_kmp.shared.generated.resources.clear
import kbbi_kmp.shared.generated.resources.clear_history_confirm_text
import kbbi_kmp.shared.generated.resources.clear_history_confirm_title
import kbbi_kmp.shared.generated.resources.clear_history_confirmed
import kbbi_kmp.shared.generated.resources.clear_search_history_title
import kbbi_kmp.shared.generated.resources.crash_reporting_description
import kbbi_kmp.shared.generated.resources.crash_reporting_title
import kbbi_kmp.shared.generated.resources.data_section_title
import kbbi_kmp.shared.generated.resources.haptic_feedback_description
import kbbi_kmp.shared.generated.resources.haptic_feedback_title
import kbbi_kmp.shared.generated.resources.interaction_section_title
import kbbi_kmp.shared.generated.resources.language_english
import kbbi_kmp.shared.generated.resources.language_indonesian
import kbbi_kmp.shared.generated.resources.language_section_title
import kbbi_kmp.shared.generated.resources.app_language_title
import kbbi_kmp.shared.generated.resources.choose_language_title
import kbbi_kmp.shared.generated.resources.notification_bookmark_review
import kbbi_kmp.shared.generated.resources.notification_bookmark_review_description
import kbbi_kmp.shared.generated.resources.notification_app_updates_title
import kbbi_kmp.shared.generated.resources.notification_app_updates_description
import kbbi_kmp.shared.generated.resources.notification_daily_proverb
import kbbi_kmp.shared.generated.resources.notification_daily_proverb_description
import kbbi_kmp.shared.generated.resources.notification_daily_word
import kbbi_kmp.shared.generated.resources.notification_daily_word_description
import kbbi_kmp.shared.generated.resources.notification_editorial_description
import kbbi_kmp.shared.generated.resources.notification_editorial_title
import kbbi_kmp.shared.generated.resources.notification_feature_unavailable
import kbbi_kmp.shared.generated.resources.notification_reminders_section
import kbbi_kmp.shared.generated.resources.notification_settings_title
import kbbi_kmp.shared.generated.resources.notification_time_label
import kbbi_kmp.shared.generated.resources.open_source_licenses_title
import kbbi_kmp.shared.generated.resources.performance_monitoring_description
import kbbi_kmp.shared.generated.resources.performance_monitoring_title
import kbbi_kmp.shared.generated.resources.platform_feature_unavailable
import kbbi_kmp.shared.generated.resources.privacy_diagnostics_section_title
import kbbi_kmp.shared.generated.resources.privacy_policy_title
import kbbi_kmp.shared.generated.resources.report_bug_title
import kbbi_kmp.shared.generated.resources.settings_title
import kbbi_kmp.shared.generated.resources.settings_menu_subtitle
import kbbi_kmp.shared.generated.resources.share_app_text
import kbbi_kmp.shared.generated.resources.share_app_title
import kbbi_kmp.shared.generated.resources.terms_condition_title
import kbbi_kmp.shared.generated.resources.theme_blaze_orange
import kbbi_kmp.shared.generated.resources.theme_deep_forest_energy
import kbbi_kmp.shared.generated.resources.theme_golden_coral_energy
import kbbi_kmp.shared.generated.resources.theme_golden_sunset
import kbbi_kmp.shared.generated.resources.theme_neon_violet
import kbbi_kmp.shared.generated.resources.theme_picker_description
import kbbi_kmp.shared.generated.resources.theme_royal_ocean
import kbbi_kmp.shared.generated.resources.usage_analytics_description
import kbbi_kmp.shared.generated.resources.usage_analytics_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenLegal: (String) -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val urlOpener: UrlOpener = koinInject()
    val share: TextShare = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val shareMessage = stringResource(Res.string.share_app_text)
    val clearedMessage = stringResource(Res.string.clear_history_confirmed)
    var confirmClearHistory by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    SettingsScreen(
        state = state,
        onBack = onBack,
        onAction = viewModel::onAction,
        onOpenLegal = onOpenLegal,
        onOpenUrl = { url ->
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                if (urlOpener.openAndAwait(url) !is CapabilityResult.Success) {
                    message = "Unable to open this link."
                }
            }
        },
        onShare = {
            if (share.share("$shareMessage\nhttps://github.com/arrazyfathan/kbbi") !is CapabilityResult.Success) {
                message =
                    "Sharing is unavailable."
            }
        },
        onClearHistory = { confirmClearHistory = true },
    )
    if (confirmClearHistory) {
        AlertDialog(
            onDismissRequest = { confirmClearHistory = false },
            title = { Text(stringResource(Res.string.clear_history_confirm_title)) },
            text = { Text(stringResource(Res.string.clear_history_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onAction(SettingsAction.ClearHistory)
                        confirmClearHistory =
                            false
                        message = clearedMessage
                    },
                ) { Text(stringResource(Res.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearHistory = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            text = { Text(text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        message = null
                    },
                ) { Text(stringResource(Res.string.cancel)) }
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsScreen(
    state: SettingsState,
    onBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    onOpenLegal: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onShare: () -> Unit,
    onClearHistory: () -> Unit,
) {
    val p = state.preferences
    var selectedTimeType by remember { mutableStateOf<ReminderType?>(null) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    val selectedReminder = selectedTimeType?.let { p.reminders.getValue(it) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsTopAppBar(scrollBehavior, onBack) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Section(stringResource(Res.string.notification_reminders_section), showHeaderDivider = false) {
                ToggleRow(
                    Res.string.notification_editorial_title,
                    Res.string.notification_editorial_description,
                    p.campaignNotificationsEnabled,
                    onChange = { onAction(SettingsAction.CampaignNotifications(it)) },
                )
                SectionDivider()
                ToggleRow(Res.string.notification_app_updates_title, Res.string.notification_app_updates_description,
                    p.updateNotificationsEnabled, onChange = { onAction(SettingsAction.UpdateNotifications(it)) })
                SectionDivider()
                ReminderType.entries.forEach { type ->
                    val pref = p.reminders.getValue(type)
                    val title =
                        when (type) {
                            ReminderType.DAILY_WORD -> Res.string.notification_daily_word
                            ReminderType.DAILY_PROVERB -> Res.string.notification_daily_proverb
                            ReminderType.BOOKMARK_REVIEW -> Res.string.notification_bookmark_review
                        }
                    val description =
                        when (type) {
                            ReminderType.DAILY_WORD -> Res.string.notification_daily_word_description
                            ReminderType.DAILY_PROVERB -> Res.string.notification_daily_proverb_description
                            ReminderType.BOOKMARK_REVIEW -> Res.string.notification_bookmark_review_description
                        }
                    val time = "${pref.hour.toString().padStart(2, '0')}:${
                        pref.minute.toString().padStart(2, '0')
                    }"
                    ReminderRow(
                        icon = when (type) {
                            ReminderType.DAILY_WORD -> Res.drawable.word
                            ReminderType.DAILY_PROVERB -> Res.drawable.ic_proverb
                            ReminderType.BOOKMARK_REVIEW -> Res.drawable.saved
                        },
                        title = title,
                        description = description,
                        enabled = pref.enabled,
                        time = time,
                        onToggle = { onAction(SettingsAction.Reminder(type, it)) },
                        onTimeClick = { selectedTimeType = type },
                    )
                    if (type != ReminderType.BOOKMARK_REVIEW) SectionDivider()
                }
            }
            AppearanceSection(
                selectedTheme = p.theme,
                selectedAppIcon = p.appIcon,
                onThemeSelected = { onAction(SettingsAction.Theme(it)) },
                onAppIconSelected = { onAction(SettingsAction.AppIcon(it)) },
            )
            Section(stringResource(Res.string.interaction_section_title)) {
                IconToggleRow(Res.drawable.ic_vibration, Res.string.haptic_feedback_title,
                    Res.string.haptic_feedback_description, p.hapticsEnabled) { onAction(SettingsAction.Haptics(it)) }
            }
            Section(stringResource(Res.string.privacy_diagnostics_section_title)) {
                IconToggleRow(Res.drawable.ic_bug, Res.string.crash_reporting_title,
                    Res.string.crash_reporting_description, p.crashReportingEnabled) { onAction(SettingsAction.CrashReporting(it)) }
                SectionDivider()
                IconToggleRow(Res.drawable.ic_analytics, Res.string.usage_analytics_title,
                    Res.string.usage_analytics_description, p.analyticsEnabled) { onAction(SettingsAction.Analytics(it)) }
                SectionDivider()
                IconToggleRow(Res.drawable.ic_speed, Res.string.performance_monitoring_title,
                    Res.string.performance_monitoring_description, p.performanceMonitoringEnabled) { onAction(SettingsAction.PerformanceMonitoring(it)) }
            }
            LanguageSection(selectedLanguage = p.language, onClick = { showLanguagePicker = true })
            Section(stringResource(Res.string.data_section_title)) {
                PreferenceRow(Res.string.clear_search_history_title, icon = Res.drawable.ic_history, trailing = "›", onClick = onClearHistory)
            }
            Section(stringResource(Res.string.about_section_title)) {
                PreferenceRow(Res.string.app_update_title, icon = Res.drawable.ic_update,
                    subtitleResource = Res.string.app_update_up_to_date, trailing = "›")
                SectionDivider()
                PreferenceRow(Res.string.app_version_title, icon = Res.drawable.ic_info, trailing = getAppVersionName())
                SectionDivider()
                PreferenceRow(Res.string.share_app_title, icon = Res.drawable.share, trailing = "›", onClick = onShare)
                SectionDivider()
                PreferenceRow(
                    Res.string.report_bug_title,
                    icon = Res.drawable.ic_bug,
                    trailing = "›",
                    onClick = { onOpenUrl("https://github.com/arrazyfathan/kbbi/issues") },
                )
                SectionDivider()
                PreferenceRow(Res.string.privacy_policy_title, icon = Res.drawable.ic_privacy, trailing = "›", onClick = { onOpenLegal("privacy") })
                SectionDivider()
                PreferenceRow(Res.string.terms_condition_title, icon = Res.drawable.ic_document, trailing = "›", onClick = { onOpenLegal("terms") })
                SectionDivider()
                PreferenceRow(
                    Res.string.open_source_licenses_title,
                    icon = Res.drawable.ic_code,
                    trailing = "›",
                    onClick = { onOpenLegal("licenses") },
                )
            }
        }
    }
    if (selectedTimeType != null && selectedReminder != null) {
        KBBITimePickerBottomSheet(
            initialHour = selectedReminder.hour,
            initialMinute = selectedReminder.minute,
            onDismissRequest = { selectedTimeType = null },
            onConfirm = { hour, minute ->
                onAction(SettingsAction.ReminderTimeChanged(selectedTimeType!!, hour, minute))
                selectedTimeType = null
            },
        )
    }
    if (showLanguagePicker) {
        LanguagePickerBottomSheet(
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
            selectedLanguage = p.language,
            isSelectionInProgress = false,
            onLanguageSelected = {
                onAction(SettingsAction.Language(it))
                showLanguagePicker = false
            },
            onDismissRequest = { showLanguagePicker = false },
        )
    }
}

@Composable
internal fun Section(
    title: String,
    showHeaderDivider: Boolean = true,
    content: @Composable () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                title,
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                style = MaterialTheme.typography.titleMedium,
                color = TextH1,
            )
            if (showHeaderDivider) SectionDivider()
            content()
        }
    }
}

@Composable
private fun ReminderRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: StringResource,
    description: StringResource,
    enabled: Boolean,
    time: String,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.Top) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else .45f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = TextH1.copy(alpha = if (enabled) 1f else .58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(stringResource(description), style = MaterialTheme.typography.bodySmall, color = TextP.copy(alpha = if (enabled) 1f else .58f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            AssistChip(onClick = onTimeClick, label = { Text(stringResource(Res.string.notification_time_label, time)) })
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = enabled, onCheckedChange = onToggle, modifier = Modifier.scale(.7f))
    }
}

@Composable
private fun IconToggleRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: StringResource,
    description: StringResource,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().toggleable(value = checked, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = onChange)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = TextH1)
            Spacer(Modifier.height(2.dp))
            Text(stringResource(description), style = MaterialTheme.typography.bodySmall, color = TextP)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = null, modifier = Modifier.scale(.7f))
    }
}

@Composable
private fun SectionDivider() = HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.background)

@Composable
private fun Section(
    title: StringResource,
    content: @Composable () -> Unit,
) {
    Section(stringResource(title), content = content)
}

@Composable
private fun PreferenceRow(
    title: StringResource,
    icon: org.jetbrains.compose.resources.DrawableResource? = null,
    subtitle: String? = null,
    subtitleResource: StringResource? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(painterResource(it), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = TextH1)
            subtitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                )
            }
            subtitleResource?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                )
            }
        }
        trailing?.let {
            Text(
                it,
                style = if (it == "›") MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
                color = if (it == "›") MaterialTheme.colorScheme.primary else TextP,
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: StringResource,
    subtitle: StringResource,
    checked: Boolean,
    enabled: Boolean = true,
    unavailableText: String? = null,
    onChange: (Boolean) -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleSmall, color = TextH1)
            Text(
                stringResource(subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
            )
            unavailableText?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(modifier = Modifier.scale(0.7f), checked = checked, enabled = enabled, onCheckedChange = onChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
) {
    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.5f

    MediumTopAppBar(
        modifier =
            Modifier.background(
                brush =
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                    ),
            ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(Res.string.settings_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = if (isCollapsed) 20.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollapsed) {
                    Text(
                        text = stringResource(Res.string.settings_menu_subtitle),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        scrollBehavior = scrollBehavior,
    )
}

