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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.components.AlertBanner
import com.ahrn.irrigatech.ui.components.BannerAlert
import com.ahrn.irrigatech.ui.components.BannerSeverity
import com.ahrn.irrigatech.ui.components.KeypadModal
import com.ahrn.irrigatech.ui.components.MetricItem
import com.ahrn.irrigatech.ui.components.MetricsGrid
import com.ahrn.irrigatech.ui.components.PumpControlCard
import com.ahrn.irrigatech.ui.components.SoilMoistureCard
import com.ahrn.irrigatech.ui.components.TopStatusBar
import com.ahrn.irrigatech.ui.components.WaterTankCard
import com.ahrn.irrigatech.ui.components.WeatherHeroCard
import com.ahrn.irrigatech.ui.components.WeatherUiState
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.theme.WeatherMood

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
    val soilTempC: Double = 24.5,
    val rain: Boolean = false,
    val keypadLocked: Boolean = true,
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
    state: DashboardUiModel = DashboardUiModel(),
    onToggleFieldPump: (Boolean) -> Unit = {},
    onToggleTankPump: (Boolean) -> Unit = {},
    onInstantStopField: () -> Unit = {},
    onInstantStopTank: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
) {
    var alertsExpanded by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }

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
                    WeatherHeroCard(state = state.weather, modifier = Modifier.fillMaxWidth())
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
                    MetricsGrid(
                        items = listOf(
                            MetricItem(
                                icon = Icons.Outlined.Thermostat,
                                label = "Soil Temp",
                                value = "${state.soilTempC}°C",
                                tint = StatusColors.warning,
                            ),
                            MetricItem(
                                icon = Icons.Outlined.ElectricBolt,
                                label = "System Voltage",
                                value = "${state.voltage}V",
                                tint = StatusColors.power,
                            ),
                            MetricItem(
                                icon = Icons.Outlined.Umbrella,
                                label = "Rain Sensor",
                                value = if (state.rain) "Rain Active" else "No Rain",
                                tint = if (state.rain) StatusColors.info else StatusColors.active,
                            ),
                            MetricItem(
                                icon = Icons.Outlined.Lock,
                                label = "Security Status",
                                value = if (state.keypadLocked) "Keypad Locked" else "Unlocked",
                                tint = StatusColors.info,
                            ),
                        ),
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
                        onToggle = { checked ->
                            if (!checked || !state.keypadLocked) {
                                onToggleFieldPump(checked)
                            } else {
                                showKeypad = true
                            }
                        },
                        onInstantStop = onInstantStopField,
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
                        onToggle = { checked ->
                            if (!checked || !state.keypadLocked) {
                                onToggleTankPump(checked)
                            } else {
                                showKeypad = true
                            }
                        },
                        onInstantStop = onInstantStopTank,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (showKeypad) {
            KeypadModal(
                title = "Confirm manual override",
                onSubmit = { pin ->
                    // TODO wire to viewModel.verifyKeypadPin(pin)
                    showKeypad = false
                },
                onDismiss = { showKeypad = false },
            )
        }
    }
}
