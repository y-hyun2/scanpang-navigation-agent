/**
 * ScanPang UI/UX Design System — ported from React Native [theme.ts].
 * Source: Figma ScanPang-UI-UX-Design (see RN theme.ts header).
 */
package com.scanpang.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────
// Color Palette (raw tokens from Figma)
// ─────────────────────────────────────────
object ScanPangPalette {
    val Blue50 = Color(0xFFE8F0FE)
    val Blue100 = Color(0xFFC3D5FC)
    val Blue500 = Color(0xFF1A73E8)
    val Blue600 = Color(0xFF1666D1)
    val Blue700 = Color(0xFF1259BA)
    val White = Color(0xFFFFFFFF)
    val Gray50 = Color(0xFFF5F6F8)
    val Gray100 = Color(0xFFE6E6E6)
    val Gray200 = Color(0xFFD2D3D8)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray600 = Color(0xFF6B7280)
    val Gray800 = Color(0xFF374151)
    val Gray900 = Color(0xFF1C1C1E)
    val Black = Color(0xFF000000)
}

// ─────────────────────────────────────────
// Semantic colors (light / dark) — mirrors theme.ts lightColors / darkColors
// ─────────────────────────────────────────
data class ScanPangSemanticColors(
    val background: Color,
    val backgroundAR: Color,
    val backgroundSurface: Color,
    val backgroundOverlay: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val surfaceDisabled: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textPlaceholder: Color,
    val textOnPrimary: Color,
    val textOnDark: Color,
    val primary: Color,
    val primaryHover: Color,
    val primaryActive: Color,
    val border: Color,
    val borderSubtle: Color,
    val iconDefault: Color,
    val iconMuted: Color,
    val iconOnPrimary: Color,
    val iconOnDark: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
)

private val LightSemantic = ScanPangSemanticColors(
    background = ScanPangPalette.White,
    backgroundAR = ScanPangPalette.Gray900,
    backgroundSurface = ScanPangPalette.Gray50,
    backgroundOverlay = ScanPangPalette.Blue50,
    surface = ScanPangPalette.White,
    surfaceSubtle = ScanPangPalette.Gray50,
    surfaceDisabled = ScanPangPalette.Gray100,
    textPrimary = ScanPangPalette.Gray900,
    textSecondary = ScanPangPalette.Gray600,
    textPlaceholder = ScanPangPalette.Gray400,
    textOnPrimary = ScanPangPalette.White,
    textOnDark = ScanPangPalette.White,
    primary = ScanPangPalette.Blue500,
    primaryHover = ScanPangPalette.Blue600,
    primaryActive = ScanPangPalette.Blue700,
    border = ScanPangPalette.Gray200,
    borderSubtle = ScanPangPalette.Gray100,
    iconDefault = ScanPangPalette.Gray800,
    iconMuted = ScanPangPalette.Gray400,
    iconOnPrimary = ScanPangPalette.White,
    iconOnDark = ScanPangPalette.White,
    success = Color(0xFF34A853),
    warning = Color(0xFFFBBF04),
    error = Color(0xFFEA4335),
    info = ScanPangPalette.Blue500,
)

private val DarkSemantic = ScanPangSemanticColors(
    background = ScanPangPalette.Gray900,
    backgroundAR = ScanPangPalette.Gray900,
    backgroundSurface = Color(0xFF2C2C2E),
    backgroundOverlay = Color(0x261A73E8),
    surface = Color(0xFF2C2C2E),
    surfaceSubtle = Color(0xFF3A3A3C),
    surfaceDisabled = Color(0xFF48484A),
    textPrimary = ScanPangPalette.White,
    textSecondary = ScanPangPalette.Gray400,
    textPlaceholder = ScanPangPalette.Gray600,
    textOnPrimary = ScanPangPalette.White,
    textOnDark = ScanPangPalette.White,
    primary = ScanPangPalette.Blue500,
    primaryHover = ScanPangPalette.Blue600,
    primaryActive = ScanPangPalette.Blue700,
    border = Color(0xFF3A3A3C),
    borderSubtle = Color(0xFF2C2C2E),
    iconDefault = ScanPangPalette.Gray200,
    iconMuted = ScanPangPalette.Gray400,
    iconOnPrimary = ScanPangPalette.White,
    iconOnDark = ScanPangPalette.White,
    success = Color(0xFF34A853),
    warning = Color(0xFFFBBF04),
    error = Color(0xFFEA4335),
    info = ScanPangPalette.Blue500,
)

fun ScanPangSemanticColors.toMaterialColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = textOnPrimary,
        primaryContainer = ScanPangPalette.Blue100,
        onPrimaryContainer = ScanPangPalette.Gray900,
        secondary = iconDefault,
        onSecondary = surface,
        tertiary = info,
        onTertiary = textOnPrimary,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceSubtle,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = borderSubtle,
        error = error,
        onError = textOnPrimary,
    )
}

fun ScanPangSemanticColors.toDarkMaterialColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = textOnPrimary,
        primaryContainer = ScanPangPalette.Blue700,
        onPrimaryContainer = ScanPangPalette.White,
        secondary = iconDefault,
        onSecondary = surface,
        tertiary = info,
        onTertiary = textOnPrimary,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceSubtle,
        onSurfaceVariant = textSecondary,
        outline = border,
        outlineVariant = borderSubtle,
        error = error,
        onError = textOnPrimary,
    )
}

// ─────────────────────────────────────────
// Typography scale (font sizes / weights) — Android uses Roboto by default
// ─────────────────────────────────────────
object ScanPangTypeScale {
    val Xs = 11.sp
    val Sm = 13.sp
    val Base = 15.sp
    val Md = 17.sp
    val Lg = 20.sp
    val Xl = 24.sp
    val Xxl = 28.sp
    val Xxxl = 34.sp

    val W400 = FontWeight.Normal
    val W500 = FontWeight.Medium
    val W600 = FontWeight.SemiBold
    val W700 = FontWeight.Bold
    val W800 = FontWeight.ExtraBold

    val LineTight = 1.2f
    val LineNormal = 1.5f
    val LineRelaxed = 1.75f

    val LetterTight = (-0.3).sp
    val LetterNormal = 0.sp
    val LetterWide = 0.5.sp
}

fun scanPangTypography(): Typography {
    val font = FontFamily.SansSerif
    return Typography(
        displayLarge = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W700,
            fontSize = ScanPangTypeScale.Xxxl,
            lineHeight = (ScanPangTypeScale.Xxxl.value * ScanPangTypeScale.LineTight).sp,
            letterSpacing = ScanPangTypeScale.LetterTight,
        ),
        titleLarge = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W700,
            fontSize = ScanPangTypeScale.Xl,
            lineHeight = (ScanPangTypeScale.Xl.value * ScanPangTypeScale.LineTight).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W600,
            fontSize = ScanPangTypeScale.Md,
            lineHeight = (ScanPangTypeScale.Md.value * ScanPangTypeScale.LineNormal).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W400,
            fontSize = ScanPangTypeScale.Base,
            lineHeight = (ScanPangTypeScale.Base.value * ScanPangTypeScale.LineNormal).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W400,
            fontSize = ScanPangTypeScale.Sm,
            lineHeight = (ScanPangTypeScale.Sm.value * ScanPangTypeScale.LineRelaxed).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W500,
            fontSize = ScanPangTypeScale.Xs,
            lineHeight = (ScanPangTypeScale.Xs.value * ScanPangTypeScale.LineRelaxed).sp,
            letterSpacing = ScanPangTypeScale.LetterWide,
        ),
        labelLarge = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W600,
            fontSize = ScanPangTypeScale.Sm,
            lineHeight = (ScanPangTypeScale.Sm.value * ScanPangTypeScale.LineNormal).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = font,
            fontWeight = ScanPangTypeScale.W500,
            fontSize = ScanPangTypeScale.Xs,
            lineHeight = (ScanPangTypeScale.Xs.value * ScanPangTypeScale.LineNormal).sp,
            letterSpacing = ScanPangTypeScale.LetterWide,
        ),
    )
}

// ─────────────────────────────────────────
// Spacing scale — theme.spacing[n] → Dp
// ─────────────────────────────────────────
object ScanPangSpacing {
    val S0: Dp = 0.dp
    val S1: Dp = 4.dp
    val S2: Dp = 8.dp
    val S3: Dp = 12.dp
    val S4: Dp = 16.dp
    val S5: Dp = 20.dp
    val S6: Dp = 24.dp
    val S7: Dp = 28.dp
    val S8: Dp = 32.dp
    val S9: Dp = 36.dp
    val S10: Dp = 40.dp
    val S12: Dp = 48.dp
    val S14: Dp = 56.dp
    val S16: Dp = 64.dp
    val S20: Dp = 80.dp
    val S24: Dp = 96.dp
}

// ─────────────────────────────────────────
// Border radius — theme.borderRadius
// ─────────────────────────────────────────
object ScanPangRadius {
    val None: Dp = 0.dp
    val Xs: Dp = 4.dp
    val Sm: Dp = 8.dp
    val Md: Dp = 12.dp
    val Lg: Dp = 16.dp
    val Xl: Dp = 20.dp
    val Xxl: Dp = 24.dp
    val Pill: Dp = 999.dp
    val Circle: Dp = 9999.dp
}

// ─────────────────────────────────────────
// Component dimensions — theme.dimensions (Dp)
// ─────────────────────────────────────────
object ScanPangDimens {
    val TabBarHeight: Dp = 95.dp
    val ChatInputHeight: Dp = 48.dp
    val IconBtnSm: Dp = 36.dp
    val IconBtnMd: Dp = 40.dp
    val IconDefault: Dp = 24.dp
    val IconSmall: Dp = 18.dp
    val PoiBadgeSize: Dp = 72.dp
    val BottomAreaHeight: Dp = 250.dp
    val FloatingPanelWidth: Dp = 361.dp
}

// ─────────────────────────────────────────
// Elevation — maps theme.shadows.*.elevation
// ─────────────────────────────────────────
object ScanPangElevation {
    val None: Dp = 0.dp
    val Sm: Dp = 2.dp
    val Md: Dp = 4.dp
    val Lg: Dp = 8.dp
}

object ScanPangZIndex {
    const val Base = 0
    const val Card = 10
    const val Overlay = 20
    const val Modal = 30
    const val Toast = 40
    const val Tooltip = 50
}

val LocalScanPangColors = staticCompositionLocalOf { LightSemantic }

object ScanPangThemeAccessor {
    val colors: ScanPangSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalScanPangColors.current
}

@Composable
fun ScanPangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val semantic = if (darkTheme) DarkSemantic else LightSemantic
    val colorScheme = if (darkTheme) {
        semantic.toDarkMaterialColorScheme()
    } else {
        semantic.toMaterialColorScheme()
    }

    CompositionLocalProvider(LocalScanPangColors provides semantic) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scanPangTypography(),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(ScanPangRadius.Xs),
                small = RoundedCornerShape(ScanPangRadius.Sm),
                medium = RoundedCornerShape(ScanPangRadius.Md),
                large = RoundedCornerShape(ScanPangRadius.Lg),
                extraLarge = RoundedCornerShape(ScanPangRadius.Xxl),
            ),
            content = content,
        )
    }
}
