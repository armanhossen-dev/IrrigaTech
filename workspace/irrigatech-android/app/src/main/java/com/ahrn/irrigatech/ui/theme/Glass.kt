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
 * Card surface: translucent gradient + soft border for the glassmorphism
 * theme, or a flat solid panel for the Pure Dark theme.
 *
 * Which style is used is driven entirely by [GlassPalette.gradient] — when
 * false (Pure Dark), this paints [GlassPalette.tint] as a flat solid color
 * with no blur, no glow, no translucency — pure minimal 2D.
 */
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(Radii.card),
    palette: GlassPalette,
    elevation: Dp = Elevation.card,
): Modifier = this
    .clip(shape)
    .then(
        if (palette.gradient) {
            Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.tint.copy(alpha = 0.14f),
                        palette.tint.copy(alpha = 0.06f),
                    ),
                ),
            )
        } else {
            Modifier.background(palette.tint)
        },
    )
    .border(width = 1.dp, color = palette.border, shape = shape)

/**
 * A soft ambient glow behind an active/running element (e.g. a pump toggle).
 * Not used by the Pure Dark theme's flat components — skip calling this
 * when [LocalFlatSurfaces] is true if you want a strictly flat 2D look there.
 */
fun Modifier.glow(color: Color, radius: Dp = 24.dp): Modifier = this
    .blur(radius)
    .background(color.copy(alpha = 0.35f))