package com.ahrn.irrigatech.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.AlertBanner
import com.ahrn.irrigatech.ui.components.BannerAlert
import com.ahrn.irrigatech.ui.components.BannerSeverity
import com.ahrn.irrigatech.ui.components.ConfirmDialog
import com.ahrn.irrigatech.ui.components.KeypadModal
import com.ahrn.irrigatech.ui.components.MetricItem
import com.ahrn.irrigatech.ui.components.PumpControlCard
import com.ahrn.irrigatech.ui.components.SoilMoistureCard
import com.ahrn.irrigatech.ui.components.TopStatusBar
import com.ahrn.irrigatech.ui.components.WaterTankCard
import com.ahrn.irrigatech.ui.components.WeatherHeroCard
import com.ahrn.irrigatech.ui.components.WeatherUiState
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.theme.WeatherMood
import com.ahrn.irrigatech.ui.util.formatTemperature
import com.ahrn.irrigatech.ui.util.temperatureUnit
import com.ahrn.irrigatech.ui.viewmodel.DashboardViewModel

/**
 * UI-only state for the redesigned dashboard. Wire this to your existing
 * DashboardViewModel: [soilMoisturePercent], [rain] and the pump on/off +
 * lock fields already exist on the current uiState (see MotorDetailScreen);
 * [weather], [tankLiters]/[tankCapacityLiters], [voltage], [batteryPercent]
 * and [soilTempC] are new fields this redesign expects the ViewModel/data
 * layer to expose (e.g. from an OpenWeatherMap repo call and the existing
 * Blynk snapshot).
 */
data class DashboardUiModel(
    val deviceName: String = "Field Controller",
    val online: Boolean = true,
    val voltage: Double = 12.6,
    val batteryPercent: Int = 88,
    val weather: WeatherUiState = WeatherUiState(
        temperatureC = 28.0,
        humidityPercent = 64,
        windKph = 11.0,
        condition = "Partly cloudy",
        mood = WeatherMood.SUNNY,
        rainForecast = listOf(10, 15, 40, 60, 30, 5),
    ),
    val soilMoisturePercent: Int = 42,
    val tankLiters: Int = 7800,
    val tankCapacityLiters: Int = 10000,
    val fieldPumpRunning: Boolean = true,
    val fieldPumpRuntimeLabel: String? = "01h 24m",
    val fieldPumpBlockedReason: String? = null,
    val tankPumpRunning: Boolean = false,
    val tankPumpBlockedReason: String? = "Tank full — auto cut-off active",
    val fieldPumpToggling: Boolean = false,
    val tankPumpToggling: Boolean = false,
    val soilTempC: Double = 24.5,
    val rain: Boolean = false,
    val useCelsius: Boolean = true,
    val keypadLocked: Boolean = true,
    val keypadError: String? = null,
    val userPhotoUrl: String? = null,
    val userDisplayName: String? = null,
    val alerts: List<BannerAlert> = listOf(
        BannerAlert(
            id = "rain-1",
            icon = Icons.Outlined.Umbrella,
            message = "Rain detected — Field pump paused automatically",
            severity = BannerSeverity.INFO,
        ),
    ),
)

@Composable
fun DashboardScreen(
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.poll()
    }

    if (state.pendingAction != null) {
        if (state.keypadLocked) {
            KeypadModal(
                title = "Unlock ${state.pendingAction!!.motor.displayName}",
                onSubmit = { viewModel.verifyKeypadPin(it) },
                onDismiss = { viewModel.dismissPending() },
                error = state.keypadError,
            )
        } else {
            ConfirmDialog(
                title = "Turn on ${state.pendingAction!!.motor.displayName}?",
                message = "Confirm before the pump starts.",
                confirmLabel = "Turn On",
                onConfirm = { viewModel.confirmPending() },
                onDismiss = { viewModel.dismissPending() },
            )
        }
    }

    val uiModel = remember(state) {
        val snap = state.snapshot
        DashboardUiModel(
            deviceName = state.device?.name ?: "Field Controller",
            online = !state.offline && !state.usingMock,
            voltage = snap?.voltage ?: 0.0,
            batteryPercent = (snap?.batteryPercent ?: 0.0).toInt(),
            soilMoisturePercent = (snap?.moisturePercent ?: 0.0).toInt(),
            tankLiters = if (snap?.tankFull == true) 10000 else 2000,
            fieldPumpRunning = snap?.fieldMotorOn ?: false,
            tankPumpRunning = snap?.tankMotorOn ?: false,
            soilTempC = snap?.temperatureC ?: 0.0,
            rain = snap?.rain ?: false,
            useCelsius = state.celsius,
            fieldPumpBlockedReason = state.lockFor(MotorId.FIELD)?.reason,
            tankPumpBlockedReason = state.lockFor(MotorId.TANK)?.reason,
            fieldPumpToggling = state.togglingMotor == MotorId.FIELD,
            tankPumpToggling = state.togglingMotor == MotorId.TANK,
            fieldPumpRuntimeLabel = null,
            keypadLocked = state.keypadLocked,
            keypadError = state.keypadError,
            userPhotoUrl = state.userPhotoUrl,
            userDisplayName = state.userDisplayName,
            alerts = state.error?.let {
                listOf(
                    BannerAlert(
                        id = "error-1",
                        icon = Icons.Outlined.Lock,
                        message = it,
                        severity = BannerSeverity.WARNING,
                    )
                )
            } ?: emptyList(),
        )
    }

    DashboardContent(
        state = uiModel,
        onToggleFieldPump = { checked ->
            viewModel.requestToggle(MotorId.FIELD)
        },
        onToggleTankPump = { checked ->
            viewModel.requestToggle(MotorId.TANK)
        },
        onInstantStopField = { viewModel.stopMotor(MotorId.FIELD) },
        onInstantStopTank = { viewModel.stopMotor(MotorId.TANK) },
        onOpenNotifications = onOpenNotifications,
        onOpenProfile = onOpenProfile,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiModel,
    onToggleFieldPump: (Boolean) -> Unit = {},
    onToggleTankPump: (Boolean) -> Unit = {},
    onInstantStopField: () -> Unit = {},
    onInstantStopTank: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    var alertsExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopStatusBar(
                deviceName = state.deviceName,
                online = state.online,
                voltage = state.voltage,
                batteryPercent = state.batteryPercent,
                unreadAlerts = state.alerts.size,
                onNotificationsClick = onOpenNotifications,
                onProfileClick = onOpenProfile,
                userPhotoUrl = state.userPhotoUrl,
                userDisplayName = state.userDisplayName,
            )

            AlertBanner(
                alerts = state.alerts,
                expanded = alertsExpanded,
                onToggleExpanded = { alertsExpanded = !alertsExpanded },
                onDismiss = { /* wire to viewModel.dismissAlert(it) */ },
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                item {
                    WeatherHeroCard(
                        state = state.weather,
                        useCelsius = state.useCelsius,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                    ) {
                        SoilMoistureCard(percent = state.soilMoisturePercent, modifier = Modifier.weight(1f))
                        WaterTankCard(
                            litersCurrent = state.tankLiters,
                            litersCapacity = state.tankCapacityLiters,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        MetricCard(
                            item = MetricItem(
                                icon = Icons.Outlined.Thermostat,
                                label = "Soil Temp",
                                value = formatTemperature(state.soilTempC, state.useCelsius) + temperatureUnit(state.useCelsius),
                                tint = StatusColors.warning,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        MetricCard(
                            item = MetricItem(
                                icon = Icons.Outlined.ElectricBolt,
                                label = "System Voltage",
                                value = "%.2fV".format(state.voltage),
                                tint = StatusColors.power,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        MetricCard(
                            item = MetricItem(
                                icon = Icons.Outlined.Umbrella,
                                label = "Rain Sensor",
                                value = if (state.rain) "Rain Active" else "No Rain",
                                tint = if (state.rain) StatusColors.info else StatusColors.active,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    SecurityStatusRow(
                        locked = state.keypadLocked,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    PumpControlCard(
                        title = "Field Irrigation Pump",
                        subtitle = "Drip line — Zone A",
                        isRunning = state.fieldPumpRunning,
                        runtimeLabel = state.fieldPumpRuntimeLabel,
                        blockedReason = state.fieldPumpBlockedReason
                            ?: if (state.rain) "Blocked automatically — rain detected" else null,
                        onToggle = onToggleFieldPump,
                        onInstantStop = onInstantStopField,
                        isLoading = state.fieldPumpToggling,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    PumpControlCard(
                        title = "Tank Pump",
                        subtitle = "Borewell feed",
                        isRunning = state.tankPumpRunning,
                        runtimeLabel = null,
                        blockedReason = state.tankPumpBlockedReason,
                        onToggle = onToggleTankPump,
                        onInstantStop = onInstantStopTank,
                        isLoading = state.tankPumpToggling,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * A single metric card: the value (number + unit) sits on its own row up
 * top, with the icon and label together on a second row underneath.
 */
@Composable
private fun MetricCard(
    item: MetricItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(horizontal = Spacing.sm, vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = item.value,
            style = MaterialTheme.typography.titleMedium,
            color = item.tint,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.tint,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SecurityStatusRow(
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = if (locked) StatusColors.info else StatusColors.active
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = tint,
        )
        Text(
            text = "Security Status",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (locked) "Keypad Locked" else "Unlocked",
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
        )
    }
}