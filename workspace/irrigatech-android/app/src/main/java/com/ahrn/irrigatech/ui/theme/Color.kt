package com.ahrn.irrigatech.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// IrrigaTech v2 — "Modern AgTech Glassmorphism" palette
// ---------------------------------------------------------------------------

// Primary — growth / agriculture
val EmeraldDeep = Color(0xFF10B981)
val MintFresh = Color(0xFF34D399)
val EmeraldDark = Color(0xFF047857)

// Secondary — water / hydration
val AquaCyan = Color(0xFF06B6D4)
val SkyBlue = Color(0xFF3B82F6)
val AquaDeep = Color(0xFF0E7490)

// Status & accent
val StatusActive = Color(0xFF22C55E)   // running / good
val StatusWarning = Color(0xFFF59E0B)  // amber
val StatusAlert = Color(0xFFEF4444)    // red
val StatusPower = Color(0xFFEAB308)    // battery / voltage yellow
val StatusInfo = AquaCyan

// Lightened variants for dark-mode legibility
val StatusActiveLight = Color(0xFF6EE7A8)
val StatusWarningLight = Color(0xFFFBC373)
val StatusAlertLight = Color(0xFFF29B9B)
val StatusPowerLight = Color(0xFFF3D673)
val StatusInfoLight = Color(0xFF7DD9EA)

// Backgrounds
val SlateUltraDark = Color(0xFF0F172A)   // dark mode base
val SlateDarkSurface = Color(0xFF162238) // dark mode elevated surface
val SlateDarkSurfaceAlt = Color(0xFF1C2B45)
val IceSlate = Color(0xFFF8FAFC)         // light mode base
val IceSlateSurface = Color(0xFFFFFFFF)  // light mode elevated surface
val IceSlateSurfaceAlt = Color(0xFFEEF2F7)

// ---------------------------------------------------------------------------
// Pure Dark — flat, minimal 2D, no-gradient theme (true grays, OLED-friendly)
// ---------------------------------------------------------------------------
val PureDarkBackground = Color(0xFF121212)   // near-black base, no tint
val PureDarkSurface = Color(0xFF1C1C1E)      // flat elevated surface
val PureDarkSurfaceAlt = Color(0xFF262628)   // flat "container" surface
val PureDarkOutline = Color(0xFF3A3A3D)
val PureDarkTextPrimary = Color(0xFFECECEC)
val PureDarkTextSecondary = Color(0xFF9A9A9E)

// Outlines / dividers
val OutlineLight = Color(0xFFDCE3EA)
val OutlineDark = Color(0xFF2A3852)

// Text
val TextPrimaryLight = Color(0xFF0B1220)
val TextSecondaryLight = Color(0xFF5B6675)
val TextPrimaryDark = Color(0xFFEAF1F8)
val TextSecondaryDark = Color(0xFF97A5BC)

// Glassmorphism surface tints (used with alpha over background gradients)
val GlassTintLight = Color(0xFFFFFFFF)
val GlassTintDark = Color(0xFF1E2A44)
val GlassBorderLight = Color(0x33FFFFFF)
val GlassBorderDark = Color(0x33AFC6E8)

// Legacy Brand colors (deprecated, used by login/splash)
val BrandGreen = Color(0xFF1B6E3C)
val BrandGreenDark = Color(0xFF14552E)

/** Weather-condition tints for the hero card's animated backdrop. */
enum class WeatherMood { SUNNY, CLOUDY, RAIN, NIGHT }

fun weatherGradient(mood: WeatherMood, dark: Boolean): List<Color> = when (mood) {
    WeatherMood.SUNNY -> if (dark) listOf(Color(0xFF1E3A5F), Color(0xFF16324D)) else listOf(SkyBlue, AquaCyan)
    WeatherMood.CLOUDY -> if (dark) listOf(Color(0xFF243044), Color(0xFF1A2436)) else listOf(Color(0xFF93A5C2), Color(0xFFB9C6DA))
    WeatherMood.RAIN -> if (dark) listOf(Color(0xFF0E2233), Color(0xFF12314A)) else listOf(AquaDeep, SkyBlue)
    WeatherMood.NIGHT -> listOf(Color(0xFF0B1424), Color(0xFF16223A))
}

// Accent alternatives offered in Settings.
data class AccentSwatch(
    val id: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
)

val AccentOceanBlue = AccentSwatch("ocean_blue", "Ocean Blue", SkyBlue, AquaCyan)
val AccentMintSky = AccentSwatch("mint_sky", "Mint Sky", MintFresh, SkyBlue)

// New vivid, modern accents — bold enough to read clearly on both the
// glassmorphism surfaces and the flat Pure Dark surfaces.
val AccentSunsetCoral = AccentSwatch("sunset_coral", "Sunset Coral", Color(0xFFFF6B5B), Color(0xFFFFA45B))
val AccentVioletNova = AccentSwatch("violet_nova", "Violet Nova", Color(0xFF8B5CF6), Color(0xFFEC4899))
val AccentCyberLime = AccentSwatch("cyber_lime", "Cyber Lime", Color(0xFF84CC16), Color(0xFF14B8A6))
val AccentCrimsonGold = AccentSwatch("crimson_gold", "Crimson Gold", Color(0xFFDC2626), Color(0xFFF59E0B))

val AccentSwatches = listOf(
    AccentSteelCopper,
    AccentOceanBlue,
    AccentMintSky,
    AccentSunsetCoral,
    AccentVioletNova,
    AccentCyberLime,
    AccentCrimsonGold,
)