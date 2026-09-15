package com.ahrn.irrigatech.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
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
 * shape AccentOceanBlue already used. Adjust the constructor call below
 * if AccentSwatch takes additional parameters in Colors.kt.
 */
val AccentSteelCopper = AccentSwatch(
    id = "steel",
    label = "Steel & Copper",
    primary = Color(0xFF3B6592),   // steel blue — primary actions, active states, brand
    secondary = Color(0xFFAD7A3C), // muted copper — secondary accents, warm counterpoint
)

/**
 * Whether the active theme should render flat, gradient-free surfaces.
 * True whenever [ThemeModeOption] resolves to dark — the dark theme is now
 * the flat, minimal "Pure Dark" look; only the light theme keeps glassmorphism.
 */
val LocalFlatSurfaces = compositionLocalOf { false }

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

/**
 * The dark theme: flat, minimal 2D, true grays, no gradients. Backgrounds
 * and surfaces are solid colors; cards render as flat panels via
 * [GlassPalette.gradient] = false (see [glassPalette] / [glassCard]).
 */
fun darkScheme(accent: AccentSwatch) = darkColorScheme(
    primary = accent.primary.lighten(),
    onPrimary = Color(0xFF0B0B0C),
    primaryContainer = PureDarkSurfaceAlt,
    onPrimaryContainer = accent.primary.lighten(),
    secondary = accent.secondary.lighten(),
    onSecondary = Color(0xFF0B0B0C),
    secondaryContainer = PureDarkSurfaceAlt,
    onSecondaryContainer = accent.secondary.lighten(),
    error = StatusAlertLight,
    background = PureDarkBackground,
    onBackground = PureDarkTextPrimary,
    surface = PureDarkSurface,
    onSurface = PureDarkTextPrimary,
    surfaceVariant = PureDarkSurfaceAlt,
    onSurfaceVariant = PureDarkTextSecondary,
    outline = PureDarkOutline,
    outlineVariant = PureDarkOutline,
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

/**
 * Glass surface tokens resolved per theme — used by [com.ahrn.irrigatech.ui.components.glassCard].
 * [gradient] is false in the dark theme, so [glassCard] paints a flat solid
 * panel instead of a translucent gradient — no blur, no glow, minimal 2D.
 * The light theme keeps the translucent glassmorphism look.
 */
data class GlassPalette(val tint: Color, val border: Color, val gradient: Boolean = true)

val glassPalette: GlassPalette
    @Composable get() = if (LocalFlatSurfaces.current) {
        GlassPalette(tint = PureDarkSurface, border = PureDarkOutline, gradient = false)
    } else {
        GlassPalette(tint = GlassTintLight, border = GlassBorderLight, gradient = true)
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

    CompositionLocalProvider(LocalFlatSurfaces provides dark) {
        MaterialTheme(
            colorScheme = scheme,
            typography = IrrigaTechTypography,
            shapes = if (dark) IrrigaTechFlatShapes else IrrigaTechShapes,
            content = content,
        )
    }
}