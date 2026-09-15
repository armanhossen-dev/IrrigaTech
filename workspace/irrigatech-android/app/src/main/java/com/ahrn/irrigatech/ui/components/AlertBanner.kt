package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.StatusColors

enum class BannerSeverity { INFO, WARNING, ALERT, GOOD }

data class BannerAlert(
    val id: String,
    val icon: ImageVector,
    val message: String,
    val severity: BannerSeverity,
)

/**
 * Expandable strip for live automation events, e.g. "Rain detected — Field
 * pump paused automatically" or "Tank level below 15%". Shows the most
 * recent alert collapsed; tap to expand the full recent list.
 */
@Composable
fun AlertBanner(
    alerts: List<BannerAlert>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (alerts.isEmpty()) return
    val top = alerts.first()
    val color = top.severity.color()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(Radii.chip))
            .clickable(onClick = onToggleExpanded)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(top.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
            Text(
                text = top.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (alerts.size > 1) {
                Text(
                    text = "+${alerts.size - 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xs))
            }
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded && alerts.size > 1,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(top = Spacing.sm)) {
                alerts.drop(1).forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            alert.icon,
                            contentDescription = null,
                            tint = alert.severity.color(),
                            modifier = Modifier.size(16.dp),
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.sm))
                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { onDismiss(alert.id) }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerSeverity.color(): Color = when (this) {
    BannerSeverity.INFO -> StatusColors.info
    BannerSeverity.GOOD -> StatusColors.active
    BannerSeverity.WARNING -> StatusColors.warning
    BannerSeverity.ALERT -> StatusColors.alert
}
