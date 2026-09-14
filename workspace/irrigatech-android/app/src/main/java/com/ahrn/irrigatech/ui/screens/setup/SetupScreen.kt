package com.ahrn.irrigatech.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahrn.irrigatech.data.model.ConnectionStatus
import com.ahrn.irrigatech.ui.AppViewModelProvider
import com.ahrn.irrigatech.ui.components.SectionHeader
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors
import com.ahrn.irrigatech.ui.viewmodel.SetupViewModel
import com.ahrn.irrigatech.ui.viewmodel.TestState

@Composable
fun SetupScreen(
    onDone: () -> Unit,
    viewModel: SetupViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val testState by viewModel.testState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Connection Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg),
        ) {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { value -> viewModel.edit { it.copy(name = value) } },
                        label = { Text("Device Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = form.token,
                        onValueChange = { value -> viewModel.edit { it.copy(token = value) } },
                        label = { Text("Blynk Auth Token") },
                        singleLine = true,
                        visualTransformation = if (form.showToken) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleTokenVisibility() }) {
                                Icon(
                                    imageVector = if (form.showToken) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (form.showToken) {
                                        "Hide token"
                                    } else {
                                        "Show token"
                                    },
                                )
                            }
                        },
                        supportingText = {
                            Text("Stored encrypted on this device only.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = form.templateId,
                        onValueChange = { value -> viewModel.edit { it.copy(templateId = value) } },
                        label = { Text("Template ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = form.location,
                        onValueChange = { value -> viewModel.edit { it.copy(location = value) } },
                        label = { Text("Farm Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                    OutlinedTextField(
                        value = form.telegramBot,
                        onValueChange = { value -> viewModel.edit { it.copy(telegramBot = value) } },
                        label = { Text("Telegram Bot") },
                        singleLine = true,
                        placeholder = { Text("@my_irrigation_bot") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            TestResultCard(testState)

            Spacer(Modifier.height(Spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = form.token.isNotBlank() && testState !is TestState.Testing,
                    modifier = Modifier.weight(1f),
                ) {
                    if (testState is TestState.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(Spacing.sm))
                    }
                    Text("Test Connection")
                }
                Button(
                    onClick = { viewModel.save { onDone() } },
                    enabled = form.canSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Device")
                }
            }

            if (devices.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.xl))
                SectionHeader(
                    title = "SAVED DEVICES",
                    modifier = Modifier.padding(horizontal = 0.dp),
                )
                devices.forEach { device ->
                    SavedDeviceRow(
                        name = device.name,
                        location = device.location,
                        active = device.id == form.id,
                        onActivate = { viewModel.loadDevice(device) },
                        onDelete = { viewModel.deleteDevice(device) },
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                TextButton(onClick = { viewModel.newDevice() }) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(Spacing.xs))
                    Text("Add another controller")
                }
            }

            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun TestResultCard(state: TestState) {
    val good = StatusColors.good
    val alert = StatusColors.alert
    when (state) {
        is TestState.Idle -> Unit
        is TestState.Testing -> Unit
        is TestState.Done -> {
            val ok = state.result.status == ConnectionStatus.OK
            Card(
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(
                    containerColor = (if (ok) good else alert).copy(alpha = 0.10f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = if (ok) good else alert,
                    )
                    Spacer(Modifier.size(Spacing.sm))
                    Text(
                        text = state.result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedDeviceRow(
    name: String,
    location: String,
    active: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (active) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onActivate, enabled = !active) {
                Text(if (active) "Editing" else "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete $name",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
