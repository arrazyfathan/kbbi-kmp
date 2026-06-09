package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kbbi_kmp.shared.generated.resources.Res
import kbbi_kmp.shared.generated.resources.inter_bold
import kbbi_kmp.shared.generated.resources.inter_extrabold
import kbbi_kmp.shared.generated.resources.inter_light
import kbbi_kmp.shared.generated.resources.inter_medium
import kbbi_kmp.shared.generated.resources.inter_regular
import kbbi_kmp.shared.generated.resources.inter_semibold
import kbbi_kmp.shared.generated.resources.inter_thin
import kbbi_kmp.shared.generated.resources.metropolis_extrabold
import kbbi_kmp.shared.generated.resources.spacegrotesk_bold
import kbbi_kmp.shared.generated.resources.spacegrotesk_light
import kbbi_kmp.shared.generated.resources.spacegrotesk_medium
import kbbi_kmp.shared.generated.resources.spacegrotesk_regular
import kbbi_kmp.shared.generated.resources.spacegrotesk_semibold
import org.jetbrains.compose.resources.Font

val InterFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold),
        Font(Res.font.inter_light, FontWeight.Light),
        Font(Res.font.inter_thin, FontWeight.Thin),
        Font(Res.font.inter_extrabold, FontWeight.ExtraBold),
    )

val MetropolisFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.metropolis_extrabold, FontWeight.ExtraBold),
    )

val SpaceGroteskFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.spacegrotesk_regular, FontWeight.Normal),
        Font(Res.font.spacegrotesk_medium, FontWeight.Medium),
        Font(Res.font.spacegrotesk_semibold, FontWeight.SemiBold),
        Font(Res.font.spacegrotesk_bold, FontWeight.Bold),
        Font(Res.font.spacegrotesk_light, FontWeight.Light),
    )

val KBBITypography: Typography
    @Composable
    get() = Typography(
        headlineLarge =
            TextStyle(
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
    )
