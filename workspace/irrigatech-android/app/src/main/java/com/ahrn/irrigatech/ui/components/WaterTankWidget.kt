package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.NumericReadout
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.theme.glassCard
import com.ahrn.irrigatech.ui.theme.glassPalette
import kotlin.math.sin

@Composable
fun WaterTankCard(
    litersCurrent: Int,
    litersCapacity: Int,
    modifier: Modifier = Modifier,
) {
    val percent = if (litersCapacity > 0) (litersCurrent * 100f / litersCapacity) else 0f
    val fillColor = when {
        percent < 15f -> StatusColors.alert
        percent < 40f -> StatusColors.warning
        else -> StatusColors.info
    }
    val palette = glassPalette

    Column(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(Radii.card), palette = palette)
            .padding(Spacing.lg),
    ) {
        Text(
            text = "WATER TANK",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.sm))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp)),
        ) {
            TankFluid(percent = percent, color = fillColor, modifier = Modifier.fillMaxWidth().height(140.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${percent.toInt()}%",
                    style = NumericReadout,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$litersCurrent L / $litersCapacity L",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (percent < 15f) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "Critically low — refill recommended",
                style = MaterialTheme.typography.labelSmall,
                color = StatusColors.alert,
            )
        }
    }
}

/** Draws a two-layer sine wave that fills upward to [percent] of the box height. */
@Composable
private fun TankFluid(percent: Float, color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing)),
        label = "wavePhase",
    )
    val animatedPercent by animateFloatAsState(
        targetValue = (percent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "tankFill",
    )

    Canvas(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
        val waterHeight = size.height * animatedPercent
        val baseY = size.height - waterHeight
        val amplitude = 6f
        val wavelength = size.width / 1.4f

        fun wavePath(offsetPhase: Float): Path = Path().apply {
            moveTo(0f, baseY)
            var x = 0f
            while (x <= size.width) {
                val y = baseY + amplitude * sin((x / wavelength) * 2 * Math.PI + offsetPhase).toFloat()
                lineTo(x, y)
                x += 4f
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(path = wavePath(phase), color = color.copy(alpha = 0.55f))
        drawPath(path = wavePath(phase + 2f), color = color.copy(alpha = 0.85f))
    }
}
