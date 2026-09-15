package com.ahrn.irrigatech.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeModeOption { LIGHT, DARK, SYSTEM }

/**
 * Default brand accent. A control-panel steel blue reads as instrumentation
 * and precision — the register this app actually operates in (pump relays,
 * voltage, sensor thresholds) — rather than the "eco/agri" green that gets
 * reached for by default and ends up meaning nothing specific. Green is
 * kept, but only where it already carries meaning: [StatusColors.active]
 * for "pump running / reading healthy."
 *
 * NOTE: assumes AccentSwatch(primary: Color, secondary: Color) — the same
 * shape AccentEmeraldAqua already used. Adjust the constructor call below
 * if AccentSwatch takes additional parameters in Colors.kt.
 */
val AccentSteelCopper = AccentSwatch(
    id = "steel",
    label = "Steel & Copper",
    primary = Color(0xFF3B6592),   // steel blue — primary actions, active states, brand
    secondary = Color(0xFFAD7A3C), // muted copper — secondary accents, warm counterpoint
)

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
    background = IceSlate,
    onBackground = TextPrimaryLight,
    surface = IceSlateSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = IceSlateSurfaceAlt,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
)

fun darkScheme(accent: AccentSwatch) = darkColorScheme(
    primary = accent.primary.lighten(),
    onPrimary = Color(0xFF0B1620),
    primaryContainer = accent.primary.copy(alpha = 0.22f),
    onPrimaryContainer = accent.primary.lighten(),
    secondary = accent.secondary.lighten(),
    onSecondary = Color(0xFF23150A),
    secondaryContainer = accent.secondary.copy(alpha = 0.22f),
    onSecondaryContainer = accent.secondary.lighten(),
    error = StatusAlertLight,
    background = SlateUltraDark,
    onBackground = TextPrimaryDark,
    surface = SlateDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateDarkSurfaceAlt,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
)

/** Blend a brand color toward white so it stays legible on charcoal/slate. */
fun Color.lighten(amount: Float = 0.35f): Color = Color(
    red = red + (1f - red) * amount,
    green = green + (1f - green) * amount,
    blue = blue + (1f - blue) * amount,
    alpha = alpha,
)

/** Resolves status colors that adapt to the active theme. */
object StatusColors {
    val active: Color @Composable get() = if (isSystemInDarkTheme()) StatusActiveLight else StatusActive
    val warning: Color @Composable get() = if (isSystemInDarkTheme()) StatusWarningLight else StatusWarning
    val alert: Color @Composable get() = if (isSystemInDarkTheme()) StatusAlertLight else StatusAlert
    val power: Color @Composable get() = if (isSystemInDarkTheme()) StatusPowerLight else StatusPower
    val info: Color @Composable get() = if (isSystemInDarkTheme()) StatusInfoLight else StatusInfo

    // Backwards-compatible alias used by existing screens (MotorDetailScreen, AlertsScreen, SetupScreen).
    val good: Color @Composable get() = active
}

/** Glass surface tokens resolved per theme — used by [com.ahrn.irrigatech.ui.components.glassCard]. */
data class GlassPalette(val tint: Color, val border: Color)

val glassPalette: GlassPalette
    @Composable get() = if (isSystemInDarkTheme()) {
        GlassPalette(tint = GlassTintDark, border = GlassBorderDark)
    } else {
        GlassPalette(tint = GlassTintLight, border = GlassBorderLight)
    }

@Composable
fun IrrigaTechTheme(
    themeMode: ThemeModeOption = ThemeModeOption.DARK, // dark is the default, premium look
    accent: AccentSwatch = AccentSteelCopper,
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