package com.arrazyfathan.kbbi.feature.settings.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.ic_arrow_back
import kbbi_kmp.shared.generated.resources.legal_effective_date
import kbbi_kmp.shared.generated.resources.navigate_back
import kbbi_kmp.shared.generated.resources.privacy_changes_body
import kbbi_kmp.shared.generated.resources.privacy_changes_title
import kbbi_kmp.shared.generated.resources.privacy_children_body
import kbbi_kmp.shared.generated.resources.privacy_children_title
import kbbi_kmp.shared.generated.resources.privacy_choices_body
import kbbi_kmp.shared.generated.resources.privacy_choices_title
import kbbi_kmp.shared.generated.resources.privacy_contact_body
import kbbi_kmp.shared.generated.resources.privacy_contact_title
import kbbi_kmp.shared.generated.resources.privacy_data_body
import kbbi_kmp.shared.generated.resources.privacy_data_title
import kbbi_kmp.shared.generated.resources.privacy_intro_body
import kbbi_kmp.shared.generated.resources.privacy_intro_title
import kbbi_kmp.shared.generated.resources.privacy_permissions_body
import kbbi_kmp.shared.generated.resources.privacy_permissions_title
import kbbi_kmp.shared.generated.resources.privacy_policy_title
import kbbi_kmp.shared.generated.resources.privacy_services_body
import kbbi_kmp.shared.generated.resources.privacy_services_title
import kbbi_kmp.shared.generated.resources.privacy_storage_body
import kbbi_kmp.shared.generated.resources.privacy_storage_title
import kbbi_kmp.shared.generated.resources.privacy_use_body
import kbbi_kmp.shared.generated.resources.privacy_use_title
import kbbi_kmp.shared.generated.resources.terms_acceptance_body
import kbbi_kmp.shared.generated.resources.terms_acceptance_title
import kbbi_kmp.shared.generated.resources.terms_changes_body
import kbbi_kmp.shared.generated.resources.terms_changes_title
import kbbi_kmp.shared.generated.resources.terms_condition_title
import kbbi_kmp.shared.generated.resources.terms_contact_body
import kbbi_kmp.shared.generated.resources.terms_contact_title
import kbbi_kmp.shared.generated.resources.terms_content_body
import kbbi_kmp.shared.generated.resources.terms_content_title
import kbbi_kmp.shared.generated.resources.terms_disclaimer_body
import kbbi_kmp.shared.generated.resources.terms_disclaimer_title
import kbbi_kmp.shared.generated.resources.terms_ownership_body
import kbbi_kmp.shared.generated.resources.terms_ownership_title
import kbbi_kmp.shared.generated.resources.terms_service_body
import kbbi_kmp.shared.generated.resources.terms_service_title
import kbbi_kmp.shared.generated.resources.terms_third_party_body
import kbbi_kmp.shared.generated.resources.terms_third_party_title
import kbbi_kmp.shared.generated.resources.terms_user_responsibilities_body
import kbbi_kmp.shared.generated.resources.terms_user_responsibilities_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class LegalDocumentSection(
    val titleRes: StringResource,
    val bodyRes: StringResource,
)

@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    LegalDocumentScreen(
        titleRes = Res.string.privacy_policy_title,
        effectiveDateRes = Res.string.legal_effective_date,
        sections = privacyPolicySections,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun TermsConditionsScreen(onNavigateBack: () -> Unit) {
    LegalDocumentScreen(
        titleRes = Res.string.terms_condition_title,
        effectiveDateRes = Res.string.legal_effective_date,
        sections = termsConditionsSections,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentScreen(
    titleRes: StringResource,
    effectiveDateRes: StringResource,
    sections: List<LegalDocumentSection>,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                modifier =
                    Modifier.background(
                        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)),
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
                    Text(
                        text = stringResource(titleRes),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontFamily = MetropolisFontFamily,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(effectiveDateRes),
                style = MaterialTheme.typography.labelMedium,
                color = TextP,
            )
            sections.forEach { section ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(section.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextH1,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(section.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextP,
                        lineHeight = 25.sp,
                    )
                }
            }
        }
    }
}

private val privacyPolicySections =
    listOf(
        LegalDocumentSection(Res.string.privacy_intro_title, Res.string.privacy_intro_body),
        LegalDocumentSection(Res.string.privacy_data_title, Res.string.privacy_data_body),
        LegalDocumentSection(Res.string.privacy_use_title, Res.string.privacy_use_body),
        LegalDocumentSection(Res.string.privacy_storage_title, Res.string.privacy_storage_body),
        LegalDocumentSection(Res.string.privacy_permissions_title, Res.string.privacy_permissions_body),
        LegalDocumentSection(Res.string.privacy_services_title, Res.string.privacy_services_body),
        LegalDocumentSection(Res.string.privacy_choices_title, Res.string.privacy_choices_body),
        LegalDocumentSection(Res.string.privacy_children_title, Res.string.privacy_children_body),
        LegalDocumentSection(Res.string.privacy_changes_title, Res.string.privacy_changes_body),
        LegalDocumentSection(Res.string.privacy_contact_title, Res.string.privacy_contact_body),
    )

private val termsConditionsSections =
    listOf(
        LegalDocumentSection(Res.string.terms_acceptance_title, Res.string.terms_acceptance_body),
        LegalDocumentSection(Res.string.terms_service_title, Res.string.terms_service_body),
        LegalDocumentSection(Res.string.terms_content_title, Res.string.terms_content_body),
        LegalDocumentSection(Res.string.terms_user_responsibilities_title, Res.string.terms_user_responsibilities_body),
        LegalDocumentSection(Res.string.terms_third_party_title, Res.string.terms_third_party_body),
        LegalDocumentSection(Res.string.terms_ownership_title, Res.string.terms_ownership_body),
        LegalDocumentSection(Res.string.terms_disclaimer_title, Res.string.terms_disclaimer_body),
        LegalDocumentSection(Res.string.terms_changes_title, Res.string.terms_changes_body),
        LegalDocumentSection(Res.string.terms_contact_title, Res.string.terms_contact_body),
    )
