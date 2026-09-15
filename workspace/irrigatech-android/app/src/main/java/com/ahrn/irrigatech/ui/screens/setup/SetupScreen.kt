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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.ahrn.irrigatech.ui.theme.Radii
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Text(
            text = "Connection setup",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.lg),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg),
        ) {
            Spacer(Modifier.height(Spacing.lg))

            // Fields sit directly on the background — the labels and
            // field borders already carry enough structure without a
            // card wrapped around them.
            OutlinedTextField(
                value = form.name,
                onValueChange = { value -> viewModel.edit { it.copy(name = value) } },
                label = { Text("Device name") },
                singleLine = true,
                shape = RoundedCornerShape(Radii.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = form.token,
                onValueChange = { value -> viewModel.edit { it.copy(token = value) } },
                label = { Text("Blynk auth token") },
                singleLine = true,
                shape = RoundedCornerShape(Radii.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
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
                shape = RoundedCornerShape(Radii.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = form.location,
                onValueChange = { value -> viewModel.edit { it.copy(location = value) } },
                label = { Text("Farm location") },
                singleLine = true,
                shape = RoundedCornerShape(Radii.button),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            TestResultRow(testState)

            Spacer(Modifier.height(Spacing.lg))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedButton(
                    onClick = { viewModel.testConnection() },
                    enabled = form.token.isNotBlank() && testState !is TestState.Testing,
                    shape = RoundedCornerShape(Radii.button),
                    modifier = Modifier.weight(1f),
                ) {
                    if (testState is TestState.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.size(Spacing.sm))
                    }
                    Text("Test connection")
                }
                Button(
                    onClick = { viewModel.save { onDone() } },
                    enabled = form.canSave,
                    shape = RoundedCornerShape(Radii.button),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save device")
                }
            }

            if (devices.isNotEmpty()) {
                Spacer(Modifier.height(Spacing.xxl))

                Text(
                    text = "Saved devices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(Spacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                devices.forEach { device ->
                    SavedDeviceRow(
                        name = device.name,
                        location = device.location,
                        active = device.id == form.id,
                        onActivate = { viewModel.loadDevice(device) },
                        onDelete = { viewModel.deleteDevice(device) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
private fun TestResultRow(state: TestState) {
    when (state) {
        is TestState.Idle -> Unit
        is TestState.Testing -> Unit
        is TestState.Done -> {
            val ok = state.result.status == ConnectionStatus.OK
            val tint = if (ok) StatusColors.active else StatusColors.alert

            Spacer(Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
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

@Composable
private fun SavedDeviceRow(
    name: String,
    location: String,
    active: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                if (active) {
                    Spacer(Modifier.size(Spacing.xs))
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Currently editing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
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