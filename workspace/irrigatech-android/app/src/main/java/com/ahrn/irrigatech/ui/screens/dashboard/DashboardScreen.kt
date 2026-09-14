package com.ahrn.irrigatech.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.SensorSnapshot
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.ConfirmDialog
import com.ahrn.irrigatech.ui.components.MetricCard
import com.ahrn.irrigatech.ui.components.OfflineBanner
import com.ahrn.irrigatech.ui.components.SectionHeader
import com.ahrn.irrigatech.ui.components.SkeletonCard
import com.ahrn.irrigatech.ui.components.StatusPill
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.util.formatPercent
import com.ahrn.irrigatech.ui.util.formatVoltage
import com.ahrn.irrigatech.ui.util.relativeTime
import com.ahrn.irrigatech.ui.util.temperatureCaption
import com.ahrn.irrigatech.ui.util.temperatureDisplay
import com.ahrn.irrigatech.ui.util.temperatureUnit
import com.ahrn.irrigatech.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOpenMotor: (MotorId) -> Unit,
    onOpenSetup: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.poll()
        }
    }

    state.pendingAction?.let { pending ->
        ConfirmDialog(
            title = "Turn on ${pending.motor.displayName}?",
            message = "Confirm before the pump starts. The motor will run until it is " +
                "switched off or a safety limit is reached.",
            confirmLabel = "Turn On",
            onConfirm = { viewModel.confirmPending() },
            onDismiss = { viewModel.dismissPending() },
        )
    }

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            DashboardHeader(
                deviceName = state.device?.name ?: "No controller",
                location = state.device?.location.orEmpty(),
                lastSynced = state.lastSyncedAt,
                offline = state.offline,
                usingMock = state.usingMock,
                onOpenSetup = onOpenSetup,
            )

            AnimatedVisibility(visible = state.offline) {
                Column {
                    Spacer(Modifier.height(Spacing.sm))
                    OfflineBanner(
                        message = "Showing the last known readings. " +
                            (state.error ?: "Controller unreachable."),
                        onRetry = { viewModel.refresh() },
                    )
                }
            }

            if (state.loading && state.snapshot == null) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    SkeletonCard(height = 108)
                    SkeletonCard(height = 108)
                    SkeletonCard(height = 132)
                }
                return@Column
            }

            val snapshot = state.snapshot
            if (snapshot == null) {
                Spacer(Modifier.height(Spacing.xxl))
                Text(
                    text = "No controller configured yet.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Spacing.xl),
                )
                return@Column
            }

            Spacer(Modifier.height(Spacing.sm))
            SectionHeader(title = "LIVE READINGS")
            SensorGrid(snapshot = snapshot, celsius = state.celsius)

            Spacer(Modifier.height(Spacing.sm))
            SectionHeader(title = "FIELD CONDITIONS")
            ConditionRow(snapshot = snapshot)

            Spacer(Modifier.height(Spacing.sm))
            SectionHeader(title = "MOTOR CONTROL")
            MotorControlCard(
                motor = MotorId.FIELD,
                snapshot = snapshot,
                lockReason = state.lockFor(MotorId.FIELD)?.reason,
                busy = state.togglingMotor == MotorId.FIELD,
                onToggle = { viewModel.requestToggle(MotorId.FIELD) },
                onOpen = { onOpenMotor(MotorId.FIELD) },
            )
            MotorControlCard(
                motor = MotorId.TANK,
                snapshot = snapshot,
                lockReason = state.lockFor(MotorId.TANK)?.reason,
                busy = state.togglingMotor == MotorId.TANK,
                onToggle = { viewModel.requestToggle(MotorId.TANK) },
                onOpen = { onOpenMotor(MotorId.TANK) },
            )

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun DashboardHeader(
    deviceName: String,
    location: String,
    lastSynced: Long?,
    offline: Boolean,
    usingMock: Boolean,
    onOpenSetup: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onOpenSetup) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Connection setup",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(Spacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (label, color) = when {
                offline -> "Offline" to StatusColors.alert
                usingMock -> "Demo data" to StatusColors.info
                else -> "Live" to StatusColors.good
            }
            StatusPill(text = label, color = color)
            Spacer(Modifier.width(Spacing.md))
            if (lastSynced != null) {
                Text(
                    text = "Synced ${relativeTime(lastSynced)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SensorGrid(snapshot: SensorSnapshot, celsius: Boolean) {
    Column(modifier = Modifier.padding(horizontal = Spacing.lg)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            MetricCard(
                label = "Temperature",
                value = snapshot.temperatureDisplay(celsius),
                unit = temperatureUnit(celsius),
                icon = Icons.Outlined.Thermostat,
                tint = StatusColors.info,
                contentDescription = stringResource(R.string.cd_temperature),
                caption = snapshot.temperatureCaption(celsius),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "Soil moisture",
                value = formatPercent(snapshot.moisturePercent),
                unit = "%",
                icon = Icons.Outlined.Opacity,
                tint = StatusColors.good,
                contentDescription = stringResource(R.string.cd_moisture),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            MetricCard(
                label = "Battery",
                value = formatPercent(snapshot.batteryPercent),
                unit = "%",
                icon = Icons.Outlined.BatteryFull,
                tint = if ((snapshot.batteryPercent ?: 100.0) <= 20) {
                    StatusColors.warning
                } else {
                    StatusColors.good
                },
                contentDescription = stringResource(R.string.cd_battery),
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = "Voltage",
                value = formatVoltage(snapshot.voltage),
                unit = "V",
                icon = Icons.Outlined.Bolt,
                tint = StatusColors.info,
                contentDescription = stringResource(R.string.cd_voltage),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConditionRow(snapshot: SensorSnapshot) {
    Row(
        modifier = Modifier.padding(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        ConditionCard(
            label = "Rain",
            value = if (snapshot.rain) "Raining" else "Dry",
            icon = Icons.Outlined.Umbrella,
            color = if (snapshot.rain) StatusColors.info else StatusColors.good,
            contentDescription = stringResource(R.string.cd_rain),
            modifier = Modifier.weight(1f),
        )
        ConditionCard(
            label = "Tank",
            value = if (snapshot.tankFull) "Full" else "Filling",
            icon = Icons.Outlined.WaterDrop,
            color = if (snapshot.tankFull) StatusColors.warning else StatusColors.info,
            contentDescription = stringResource(R.string.cd_tank),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConditionCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun MotorControlCard(
    motor: MotorId,
    snapshot: SensorSnapshot,
    lockReason: String?,
    busy: Boolean,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    val running = snapshot.isOn(motor)
    val accent = if (running) StatusColors.good else MaterialTheme.colorScheme.onSurfaceVariant
    val transition = rememberInfiniteTransition(label = "motor-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "motor-pulse-alpha",
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .alpha(if (running) pulse else 0.35f)
                        .background(accent, RoundedCornerShape(50)),
                )
                Spacer(Modifier.width(Spacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = motor.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = when {
                            running -> "Running"
                            lockReason != null -> "Blocked"
                            else -> "Stopped"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = accent,
                    )
                }
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Switch(
                        checked = running,
                        onCheckedChange = { onToggle() },
                        enabled = lockReason == null,
                    )
                }
            }

            if (lockReason != null) {
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            StatusColors.warning.copy(alpha = 0.12f),
                            MaterialTheme.shapes.small,
                        )
                        .padding(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(StatusColors.warning, RoundedCornerShape(50)),
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text(
                        text = lockReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "View details",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen)
                    .padding(vertical = Spacing.xs),
            )
        }
    }
}
