package com.ahrn.irrigatech.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.R
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.SectionHeader
import com.ahrn.irrigatech.ui.theme.AccentSwatches
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.ThemeModeOption
import com.ahrn.irrigatech.ui.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    onOpenSetup: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val signingOut by viewModel.signingOut.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val container = (context.applicationContext as IrrigaTechApp).container

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        )

        SectionHeader(title = "APPEARANCE")
        SettingsCard {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(Spacing.sm))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeModeOption.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = settings.themeMode == option,
                        onClick = { viewModel.setTheme(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeModeOption.entries.size,
                        ),
                    ) {
                        Text(option.label())
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = "Accent colour",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                AccentSwatches.forEach { swatch ->
                    val selected = settings.accentId == swatch.id
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    shape = CircleShape,
                                )
                                .padding(4.dp)
                                .background(swatch.primary, CircleShape)
                                .clickable { viewModel.setAccent(swatch.id) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = swatch.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        SectionHeader(title = "DATA & UNITS")
        SettingsCard {
            Text(
                text = "Refresh every ${settings.pollSeconds} seconds",
                style = MaterialTheme.typography.bodyLarge,
            )
            Slider(
                value = settings.pollSeconds.toFloat(),
                onValueChange = { viewModel.setPollSeconds(it.toInt()) },
                valueRange = 5f..30f,
                steps = 24,
            )
            ToggleRow(
                title = "Temperature in Celsius",
                subtitle = "Off uses Fahrenheit",
                checked = settings.useCelsius,
                onCheckedChange = { viewModel.setCelsius(it) },
            )
        }

        SectionHeader(title = "NOTIFICATIONS")
        SettingsCard {
            ToggleRow(
                title = "Tank full",
                subtitle = "Alert when the tank stops filling",
                checked = settings.notifyTankFull,
                onCheckedChange = { viewModel.setNotifyTankFull(it) },
            )
            ToggleRow(
                title = "Rain detection",
                subtitle = "Alert when rain blocks the field motor",
                checked = settings.notifyRain,
                onCheckedChange = { viewModel.setNotifyRain(it) },
            )
            ToggleRow(
                title = "Motor auto stop",
                subtitle = "Alert on automatic motor changes",
                checked = settings.notifyMotorAutoStop,
                onCheckedChange = { viewModel.setNotifyMotorAutoStop(it) },
            )
            ToggleRow(
                title = "Low battery",
                subtitle = "Alert when the battery drops below 20%",
                checked = settings.notifyLowBattery,
                onCheckedChange = { viewModel.setNotifyLowBattery(it) },
            )
        }

        SectionHeader(title = "CONTROLLERS")
        SettingsCard {
            if (devices.isEmpty()) {
                Text(
                    text = "No controllers saved yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            devices.forEach { device ->
                DeviceRow(
                    device = device,
                    onClick = {
                        viewModel.activate(device)
                        onOpenSetup()
                    },
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            TextButton(onClick = onOpenSetup) {
                Icon(
                    imageVector = Icons.Outlined.Devices,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text("Manage connections")
            }
        }

        SectionHeader(title = "ACCOUNT")
        SettingsCard {
            val session by container.sessionStore.user.collectAsStateWithLifecycle(initialValue = null)
            Text(
                text = session?.displayName ?: "Signed in",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            session?.email?.takeIf { it.isNotBlank() }?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (session?.isDemo == true) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Demo profile (Firebase not configured)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Spacing.md))
            TextButton(
                onClick = { viewModel.signOut { onSignedOut() } },
                enabled = !signingOut,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(Spacing.xs))
                Text("Sign out")
            }
        }

        SectionHeader(title = "ABOUT")
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAbout)
                    .padding(vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Text(
                    text = "About ${stringResource(R.string.app_name)}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) { content() }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DeviceRow(device: DeviceConfig, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Devices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.name, style = MaterialTheme.typography.bodyLarge)
            if (device.location.isNotBlank()) {
                Text(
                    text = device.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "Edit",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun ThemeModeOption.label(): String = when (this) {
    ThemeModeOption.LIGHT -> "Light"
    ThemeModeOption.DARK -> "Dark"
    ThemeModeOption.SYSTEM -> "System"
}
