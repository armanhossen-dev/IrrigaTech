package com.ahrn.irrigatech.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.data.model.AlertEvent
import com.ahrn.irrigatech.data.model.AlertKind
import com.ahrn.irrigatech.data.model.AlertSeverity
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.EmptyState
import com.ahrn.irrigatech.ui.components.SectionHeader
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.util.formatDateTime
import com.ahrn.irrigatech.ui.viewmodel.AlertsViewModel

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.filtered.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Alerts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (state.alerts.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text("Clear")
                }
            }
        }

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                FilterChip(
                    selected = state.filter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("All") },
                )
            }
            items(AlertKind.entries.toList()) { kind ->
                FilterChip(
                    selected = state.filter == kind,
                    onClick = { viewModel.setFilter(kind) },
                    label = { Text(kind.label()) },
                )
            }
        }

        if (state.alerts.isEmpty()) {
            Spacer(Modifier.height(Spacing.xxl))
            EmptyState(
                icon = Icons.Outlined.NotificationsNone,
                title = "No alerts yet",
                message = "Motor, rain, tank and battery events will appear here.",
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.md,
                bottom = Spacing.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(state.alerts, key = { it.id }) { alert ->
                AlertRow(alert)
            }
        }
    }
}

@Composable
private fun AlertRow(alert: AlertEvent) {
    val tint = alert.severity.color()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = alert.kind.icon(),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = alert.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = formatDateTime(alert.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun AlertKind.label(): String = when (this) {
    AlertKind.MOTOR -> "Motors"
    AlertKind.RAIN -> "Rain"
    AlertKind.TANK -> "Tank"
    AlertKind.BATTERY -> "Battery"
    AlertKind.SECURITY -> "Security"
    AlertKind.SYSTEM -> "System"
}

private fun AlertKind.icon(): ImageVector = when (this) {
    AlertKind.MOTOR -> Icons.Outlined.ElectricBolt
    AlertKind.RAIN -> Icons.Outlined.Umbrella
    AlertKind.TANK -> Icons.Outlined.WaterDrop
    AlertKind.BATTERY -> Icons.Outlined.BatteryAlert
    AlertKind.SECURITY -> Icons.Outlined.Security
    AlertKind.SYSTEM -> Icons.Outlined.Info
}

@Composable
private fun AlertSeverity.color(): Color = when (this) {
    AlertSeverity.INFO -> StatusColors.info
    AlertSeverity.GOOD -> StatusColors.good
    AlertSeverity.WARNING -> StatusColors.warning
    AlertSeverity.ALERT -> StatusColors.alert
}
