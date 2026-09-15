package com.ahrn.irrigatech.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Rounded, soft-elevation shapes for the v2 glassmorphism system.
val IrrigaTechShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
}

/** Radii reused by custom-drawn components (gauges, tank, glass cards). */
object Radii {
    val card = 24.dp
    val heroCard = 28.dp
    val chip = 999.dp
    val button = 14.dp
    val gaugeStroke = 14.dp
}

/** Elevation tokens for soft-card shadows over dark/light backgrounds. */
object Elevation {
    val flat = 0.dp
    val card = 0.dp
    val raised = 0.dp
    val floating = 0.dp // bottom nav, FABs, modals
}
