package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.NumericReadoutLarge
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.theme.glassCard
import com.ahrn.irrigatech.ui.theme.glassPalette

enum class MoistureZone { DRY, OPTIMAL, WET }

fun moistureZoneFor(percent: Int): MoistureZone = when {
    percent < 30 -> MoistureZone.DRY
    percent <= 65 -> MoistureZone.OPTIMAL
    else -> MoistureZone.WET
}

private fun MoistureZone.label(): String = when (this) {
    MoistureZone.DRY -> "Dry"
    MoistureZone.OPTIMAL -> "Optimal"
    MoistureZone.WET -> "Wet"
}

@Composable
private fun MoistureZone.color(): Color = when (this) {
    MoistureZone.DRY -> StatusColors.warning
    MoistureZone.OPTIMAL -> StatusColors.active
    MoistureZone.WET -> StatusColors.info
}

/**
 * Soil moisture card with a 270° radial gradient arc gauge, e.g. "42% —
 * Optimal", color-coded dry/optimal/wet.
 */
@Composable
fun SoilMoistureCard(percent: Int, modifier: Modifier = Modifier) {
    val zone = moistureZoneFor(percent)
    val zoneColor = zone.color()
    val palette = glassPalette

    Column(
        modifier = modifier
            .glassCard(shape = androidx.compose.foundation.shape.RoundedCornerShape(Radii.card), palette = palette)
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SOIL MOISTURE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
            RadialGauge(
                percent = percent,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                progressColor = zoneColor,
                modifier = Modifier.size(148.dp),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$percent%", style = NumericReadoutLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    text = zone.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = zoneColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** A generic 270°-sweep radial gauge reused by other metric cards. */
@Composable
fun RadialGauge(
    percent: Int,
    trackColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = Radii.gaugeStroke,
) {
    val animated by animateFloatAsState(
        targetValue = percent.coerceIn(0, 100) / 100f,
        animationSpec = tween(700),
        label = "gauge",
    )
    val startAngle = 135f
    val sweepMax = 270f

    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val inset = strokeWidth.toPx() / 2
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = sweepMax,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(progressColor.copy(alpha = 0.4f), progressColor),
            ),
            startAngle = startAngle,
            sweepAngle = sweepMax * animated,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
    }
}
