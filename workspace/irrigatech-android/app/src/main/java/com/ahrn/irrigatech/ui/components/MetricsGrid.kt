package com.ahrn.irrigatech.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.NumericReadoutSmall
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.glassCard
import com.ahrn.irrigatech.ui.theme.glassPalette

data class MetricItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val tint: Color,
)

/** 2x2 grid of compact telemetry cards: soil temp, voltage, rain, security. */
@Composable
fun MetricsGrid(items: List<MetricItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                rowItems.forEach { item ->
                    MetricTile(item = item, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricTile(item: MetricItem, modifier: Modifier = Modifier) {
    val palette = glassPalette
    Row(
        modifier = modifier
            .glassCard(shape = RoundedCornerShape(Radii.card), palette = palette)
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.tint,
            modifier = Modifier.size(32.dp)
        )
        Column {
            Text(
                text = item.value,
                style = NumericReadoutSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
