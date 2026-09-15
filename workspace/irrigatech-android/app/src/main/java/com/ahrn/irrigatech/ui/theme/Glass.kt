package com.ahrn.irrigatech.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Frosted-glass card surface: translucent tint + soft border.
 * Falls back gracefully on devices where background blur isn't available —
 * the translucency + gradient alone reads as "glass" over the app's dark
 * slate / soft ice backgrounds.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(Radii.card),
    palette: GlassPalette,
    elevation: Dp = Elevation.card,
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            colors = listOf(
                palette.tint.copy(alpha = 0.14f),
                palette.tint.copy(alpha = 0.06f),
            ),
        ),
    )
    .border(width = 1.dp, color = palette.border, shape = shape)

/** A soft ambient glow behind an active/running element (e.g. a pump toggle). */
fun Modifier.glow(color: Color, radius: Dp = 24.dp): Modifier = this
    .blur(radius)
    .background(color.copy(alpha = 0.35f))
