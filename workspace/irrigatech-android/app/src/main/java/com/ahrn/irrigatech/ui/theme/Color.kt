package com.ahrn.irrigatech.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette derived from the IrrigaTech logo.
val BrandGreen = Color(0xFF1B6E3C)
val BrandGreenDark = Color(0xFF14552E)
val BrandGreenLight = Color(0xFF4CAF70)
val SkyBlue = Color(0xFF2196F3)
val SkyBlueDark = Color(0xFF1565C0)
val SkyBlueLight = Color(0xFF64B5F6)

// Neutral surfaces: white / charcoal, per the visual rules.
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightVariant = Color(0xFFF4F6F8)
val SurfaceDark = Color(0xFF121417)
val SurfaceDarkVariant = Color(0xFF1C2024)
val OutlineLight = Color(0xFFDDE2E7)
val OutlineDark = Color(0xFF2E343A)

// Status colors: green good, amber warning, red alert, blue info.
val StatusGood = Color(0xFF1B8A4B)
val StatusWarning = Color(0xFFB26A00)
val StatusAlert = Color(0xFFC62828)
val StatusInfo = Color(0xFF1565C0)
val StatusGoodLight = Color(0xFF7FD49F)
val StatusWarningLight = Color(0xFFE8B266)
val StatusAlertLight = Color(0xFFE88A8A)
val StatusInfoLight = Color(0xFF7FB3E8)

// Accent alternatives offered in Settings.
data class AccentSwatch(
    val id: String,
    val label: String,
    val primary: Color,
    val secondary: Color,
)

val AccentGreenBlue = AccentSwatch("green_blue", "Green Blue", BrandGreen, SkyBlue)
val AccentOcean = AccentSwatch("ocean", "Ocean Blue", SkyBlue, Color(0xFF00A0A0))
val AccentEarth = AccentSwatch("earth", "Earth Green", BrandGreen, Color(0xFF7A8B2E))

val AccentSwatches = listOf(AccentGreenBlue, AccentOcean, AccentEarth)
