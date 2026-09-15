package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors

/**
 * Live status header: hardware connection pulse, voltage/battery readout,
 * profile avatar, and a notification bell with an unread badge.
 */
@Composable
fun TopStatusBar(
    deviceName: String,
    online: Boolean,
    voltage: Double?,
    batteryPercent: Int?,
    unreadAlerts: Int,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseDot(active = online)
            Spacer(Spacing.sm)
            androidx.compose.foundation.layout.Column {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (online) "ESP32 Online" else "ESP32 Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (online) StatusColors.active else StatusColors.alert,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (voltage != null || batteryPercent != null) {
                PowerChip(voltage = voltage, batteryPercent = batteryPercent)
                Spacer(Spacing.sm)
            }
            NotificationBell(unreadAlerts = unreadAlerts, onClick = onNotificationsClick)
            Spacer(Spacing.sm)
            ProfileAvatar(onClick = onProfileClick)
        }
    }
}

@Composable
private fun RowScope.Spacer(width: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.size(width))
}

@Composable
private fun PulseDot(active: Boolean) {
    val color = if (active) StatusColors.active else StatusColors.alert
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (active) 1.8f else 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulseScale",
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color.copy(alpha = 0.25f / scale.coerceAtLeast(1f)), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
    }
}

@Composable
private fun PowerChip(voltage: Double?, batteryPercent: Int?) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.BatteryChargingFull,
            contentDescription = null,
            tint = StatusColors.power,
            modifier = Modifier.size(16.dp),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xs))
        val label = buildString {
            if (voltage != null) append("%.1fV".format(voltage))
            if (voltage != null && batteryPercent != null) append(" · ")
            if (batteryPercent != null) append("$batteryPercent%")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun NotificationBell(unreadAlerts: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
        if (unreadAlerts > 0) {
            Box(
                modifier = Modifier
                    .padding(bottom = 18.dp, start = 18.dp)
                    .size(16.dp)
                    .background(StatusColors.alert, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unreadAlerts > 9) "9+" else unreadAlerts.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "A",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
