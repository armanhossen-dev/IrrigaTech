package com.ahrn.irrigatech.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors

/**
 * Full-screen dim overlay with a glass keypad card for authorized manual
 * overrides on-site (e.g. unlocking a motor toggle that's normally
 * automation-only). [pinLength] dots are shown as filled once entered.
 */
@Composable
fun KeypadModal(
    title: String = "Enter security PIN",
    pinLength: Int = 4,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    error: String? = null,
) {
    var entered by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(Radii.heroCard),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = StatusColors.info,
                    modifier = Modifier.size(28.dp),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.md))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.lg))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    repeat(pinLength) { index ->
                        val filled = index < entered.length
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    CircleShape,
                                ),
                        )
                    }
                }

                if (error != null) {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
                    Text(text = error, style = MaterialTheme.typography.bodySmall, color = StatusColors.alert)
                }

                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xl))

                KeypadGrid(
                    onDigit = { digit ->
                        if (entered.length < pinLength) {
                            entered += digit
                            if (entered.length == pinLength) {
                                onSubmit(entered)
                            }
                        }
                    },
                    onBackspace = { entered = entered.dropLast(1) },
                )
            }
        }
    }
}

@Composable
private fun KeypadGrid(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                row.forEach { key ->
                    KeypadKey(
                        label = key,
                        onClick = {
                            when (key) {
                                "" -> Unit
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(label: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
                if (label.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else androidx.compose.ui.graphics.Color.Transparent,
                CircleShape,
            )
            .let { if (label.isNotEmpty()) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        if (label == "⌫") {
            Icon(Icons.Outlined.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface)
        } else if (label.isNotEmpty()) {
            Text(text = label, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

