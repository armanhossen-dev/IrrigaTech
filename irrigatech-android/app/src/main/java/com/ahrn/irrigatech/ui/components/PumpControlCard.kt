package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.theme.glassCard
import com.ahrn.irrigatech.ui.theme.glassPalette

/**
 * A dual-pump control card (Field Irrigation or Tank Pump). When [isRunning]
 * is true it shows an emerald glow, animated water-flow lines and a runtime
 * timer; otherwise a plain switch to start it (disabled + reason shown when
 * [blockedReason] is non-null, e.g. rain lockout or tank-full cutoff).
 */
@Composable
fun PumpControlCard(
    title: String,
    subtitle: String,
    isRunning: Boolean,
    runtimeLabel: String?,
    blockedReason: String?,
    onToggle: (Boolean) -> Unit,
    onInstantStop: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val palette = glassPalette
    val accent = if (isRunning) StatusColors.active else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(Radii.card), palette = palette)
            .padding(Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(4.dp),
                    strokeWidth = 2.dp,
                    color = StatusColors.info,
                )
            } else {
                Switch(
                    checked = isRunning,
                    onCheckedChange = onToggle,
                    enabled = true,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = StatusColors.active,
                        checkedThumbColor = Color.White,
                    ),
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.md))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accent.copy(alpha = if (isRunning) 0.14f else 0.06f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isRunning) {
                FlowLines(color = accent, modifier = Modifier.fillMaxWidth().height(64.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PumpStatusDot(active = isRunning)
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
                Text(
                    text = when {
                        isRunning -> "Running${runtimeLabel?.let { " · $it" } ?: ""}"
                        blockedReason != null -> "Blocked"
                        else -> "Stopped"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
        }

        if (blockedReason != null && !isRunning) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.sm))
            Text(
                text = blockedReason,
                style = MaterialTheme.typography.labelSmall,
                color = StatusColors.warning,
            )
        }

        if (isRunning) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.md))
            androidx.compose.material3.OutlinedButton(
                onClick = onInstantStop,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = StatusColors.alert,
                ),
            ) {
                Icon(Icons.Outlined.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
                Text("Instant stop")
            }
        }
    }
}

@Composable
private fun PumpStatusDot(active: Boolean) {
    val transition = rememberInfiniteTransition(label = "pumpPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pumpPulseAlpha",
    )
    val color = if (active) StatusColors.active else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color.copy(alpha = if (active) alpha else 0.6f), CircleShape),
    )
}

/** Animated horizontal flow lines suggesting water movement while a pump runs. */
@Composable
private fun FlowLines(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "flow")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = androidx.compose.animation.core.LinearEasing)),
        label = "flowShift",
    )
    Canvas(modifier = modifier) {
        val rowCount = 3
        val rowHeight = size.height / rowCount
        for (row in 0 until rowCount) {
            val y = rowHeight * row + rowHeight / 2
            val dashLength = 28f
            val gap = 20f
            var x = -((shift * (dashLength + gap)) % (dashLength + gap)) - (row * 14f)
            while (x < size.width) {
                drawLine(
                    color = color.copy(alpha = 0.30f),
                    start = Offset(x, y),
                    end = Offset(x + dashLength, y),
                    strokeWidth = 4f,
                )
                x += dashLength + gap
            }
        }
    }
}
