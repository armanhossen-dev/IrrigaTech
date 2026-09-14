package com.ahrn.irrigatech.ui.screens.motor

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.ConfirmDialog
import com.ahrn.irrigatech.ui.components.InfoRow
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.util.formatPercent
import com.ahrn.irrigatech.ui.util.relativeTime
import com.ahrn.irrigatech.ui.viewmodel.DashboardViewModel

@Composable
fun MotorDetailScreen(
    motor: MotorId,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = state.snapshot
    val running = snapshot?.isOn(motor) ?: false
    val lock = state.lockFor(motor)

    state.pendingAction?.let { pending ->
        ConfirmDialog(
            title = "Turn on ${pending.motor.displayName}?",
            message = "Confirm before the pump starts.",
            confirmLabel = "Turn On",
            onConfirm = { viewModel.confirmPending() },
            onDismiss = { viewModel.dismissPending() },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                text = motor.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val tint = when {
                    running -> StatusColors.good
                    lock != null -> StatusColors.warning
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(tint.copy(alpha = 0.14f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ElectricBolt,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = when {
                        running -> "Running"
                        lock != null -> "Blocked by safety"
                        else -> "Stopped"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                )
                if (state.lastSyncedAt != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Updated ${relativeTime(state.lastSyncedAt!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(Spacing.xl))
                if (running) {
                    OutlinedButton(
                        onClick = { viewModel.requestToggle(motor) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Stop motor")
                    }
                } else {
                    Button(
                        onClick = { viewModel.requestToggle(motor) },
                        enabled = lock == null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Start motor")
                    }
                }
            }
        }

        if (lock != null) {
            Spacer(Modifier.height(Spacing.lg))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = StatusColors.warning.copy(alpha = 0.12f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = StatusColors.warning,
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Text(
                        text = lock.reason,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(
                    text = "Safety limits",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.sm))
                InfoRow(
                    label = "Rain status",
                    value = if (snapshot?.rain == true) "Raining" else "Dry",
                    valueColor = if (snapshot?.rain == true) StatusColors.info else StatusColors.good,
                )
                InfoRow(
                    label = "Tank status",
                    value = if (snapshot?.tankFull == true) "Full" else "Filling",
                    valueColor = if (snapshot?.tankFull == true) {
                        StatusColors.warning
                    } else {
                        StatusColors.info
                    },
                )
                InfoRow(
                    label = "Tank motor",
                    value = if (snapshot?.tankMotorOn == true) "Running" else "Stopped",
                )
                InfoRow(
                    label = "Field motor",
                    value = if (snapshot?.fieldMotorOn == true) "Running" else "Stopped",
                )
                InfoRow(
                    label = "Moisture",
                    value = "${formatPercent(snapshot?.moisturePercent)}%",
                )
            }
        }

        Spacer(Modifier.height(Spacing.lg))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(Modifier.width(Spacing.md))
                Text(
                    text = if (motor == MotorId.FIELD) {
                        "The field motor is blocked automatically whenever rain is " +
                            "detected, so water is never wasted."
                    } else {
                        "The tank motor stops automatically once the tank is full and " +
                            "resumes when the level drops."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}
