package com.ahrn.irrigatech.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeModeOption { LIGHT, DARK, SYSTEM }

fun lightScheme(accent: AccentSwatch) = lightColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
    primaryContainer = accent.primary.copy(alpha = 0.12f),
    onPrimaryContainer = accent.primary,
    secondary = accent.secondary,
    onSecondary = Color.White,
    secondaryContainer = accent.secondary.copy(alpha = 0.12f),
    onSecondaryContainer = accent.secondary,
    error = StatusAlert,
    background = SurfaceLightVariant,
    onBackground = Color(0xFF15181B),
    surface = SurfaceLight,
    onSurface = Color(0xFF15181B),
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = Color(0xFF5C646B),
    outline = OutlineLight,
    outlineVariant = OutlineLight,
)

fun darkScheme(accent: AccentSwatch) = darkColorScheme(
    primary = accent.primary.lighten(),
    onPrimary = Color(0xFF102016),
    primaryContainer = accent.primary.copy(alpha = 0.24f),
    onPrimaryContainer = accent.primary.lighten(),
    secondary = accent.secondary.lighten(),
    onSecondary = Color(0xFF0E1620),
    secondaryContainer = accent.secondary.copy(alpha = 0.24f),
    onSecondaryContainer = accent.secondary.lighten(),
    error = StatusAlertLight,
    background = SurfaceDark,
    onBackground = Color(0xFFE6E9EC),
    surface = SurfaceDarkVariant,
    onSurface = Color(0xFFE6E9EC),
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = Color(0xFFAAB2BA),
    outline = OutlineDark,
    outlineVariant = OutlineDark,
)

/** Blend a brand color toward white so it stays legible on charcoal. */
fun Color.lighten(amount: Float = 0.35f): Color = Color(
    red = red + (1f - red) * amount,
    green = green + (1f - green) * amount,
    blue = blue + (1f - blue) * amount,
    alpha = alpha,
)

/** Resolves status colors that adapt to the active theme. */
object StatusColors {
    val good: Color @Composable get() = if (isSystemInDarkTheme()) StatusGoodLight else StatusGood
    val warning: Color @Composable get() = if (isSystemInDarkTheme()) StatusWarningLight else StatusWarning
    val alert: Color @Composable get() = if (isSystemInDarkTheme()) StatusAlertLight else StatusAlert
    val info: Color @Composable get() = if (isSystemInDarkTheme()) StatusInfoLight else StatusInfo
}

@Composable
fun IrrigaTechTheme(
    themeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    accent: AccentSwatch = AccentGreenBlue,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeModeOption.LIGHT -> false
        ThemeModeOption.DARK -> true
        ThemeModeOption.SYSTEM -> isSystemInDarkTheme()
    }
    val scheme = if (dark) darkScheme(accent) else lightScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = IrrigaTechTypography,
        shapes = IrrigaTechShapes,
        content = content,
    )
}
