package com.arrazyfathan.kbbi.feature.settings.presentation.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.ic_arrow_back
import kbbi_kmp.shared.generated.resources.navigate_back
import kbbi_kmp.shared.generated.resources.open_source_licenses_title
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryBadges

@Composable
fun OpenSourceLicensesScreen(onNavigateBack: () -> Unit) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }

    OpenSourceLicensesContent(
        libraries = libraries,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenSourceLicensesContent(
    libraries: Libs?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier =
                    Modifier.background(
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
                    Text(
                        text = stringResource(Res.string.open_source_licenses_title),
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
        LibrariesContainer(
            modifier = Modifier.fillMaxSize().padding(
                PaddingValues(
                    start = padding.calculateStartPadding(layoutDirection),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(layoutDirection),
                ),
            ),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            libraries = libraries,
            variant = LibrariesVariant.Traditional,
            badges = LibraryBadges(license = false),
            divider = {
                HorizontalDivider(
                    thickness = 0.5.dp,
                )
            },
            variantColors =
                LibraryDefaults.m3VariantColors(
                    rowExpandedBackground = Color.White,
                ),
        )
    }
}

