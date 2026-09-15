package com.ahrn.irrigatech.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahrn.irrigatech.ui.theme.Radii
import com.ahrn.irrigatech.ui.theme.Spacing
import com.ahrn.irrigatech.ui.theme.WeatherMood
import com.ahrn.irrigatech.ui.theme.glassCard
import com.ahrn.irrigatech.ui.theme.glassPalette
import kotlin.random.Random

data class WeatherUiState(
    val temperatureC: Double,
    val humidityPercent: Int,
    val windKph: Double,
    val condition: String,
    val mood: WeatherMood,
    val rainForecast: List<Int>, // hourly rain-probability %, next few hours
)

@Composable
fun WeatherHeroCard(state: WeatherUiState, modifier: Modifier = Modifier) {
    val palette = glassPalette
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .glassCard(shape = RoundedCornerShape(Radii.heroCard), palette = palette),
    ) {
        WeatherBackdrop(mood = state.mood, modifier = Modifier.fillMaxWidth().height(110.dp))

        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${state.temperatureC.toInt()}°C",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.condition,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    ConditionRow(icon = Icons.Outlined.WaterDrop, label = "${state.humidityPercent}%")
                    androidx.compose.foundation.layout.Spacer(Modifier.height(Spacing.xs))
                    ConditionRow(icon = Icons.Outlined.Air, label = "${state.windKph.toInt()} km/h")
                }
            }
        }
    }
}

@Composable
private fun ConditionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.xs))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
    }
}

/**
 * Lightweight animated backdrop matching the current weather mood: drifting
 * cloud blobs for CLOUDY/SUNNY, soft rain streaks for RAIN, and a starfield
 * for NIGHT. Deterministic seeds keep it stable across recompositions.
 */
@Composable
private fun WeatherBackdrop(mood: WeatherMood, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "weather")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "drift",
    )

    val colors = com.ahrn.irrigatech.ui.theme.weatherGradient(mood, dark = true)

    Canvas(modifier = modifier) {
        drawRect(brush = Brush.linearGradient(colors))

        when (mood) {
            WeatherMood.RAIN -> {
                val rng = Random(42)
                repeat(28) { i ->
                    val baseX = rng.nextFloat() * size.width
                    val y = ((drift + i / 28f) % 1f) * size.height
                    drawLine(
                        color = Color.White.copy(alpha = 0.18f),
                        start = Offset(baseX, y),
                        end = Offset(baseX - 6f, y + 22f),
                        strokeWidth = 2f,
                    )
                }
            }
            WeatherMood.SUNNY -> {
                val cx = size.width * 0.82f
                val cy = size.height * 0.28f
                rotate(degrees = drift * 360f, pivot = Offset(cx, cy)) {
                    repeat(10) { i ->
                        val angle = (i / 10f) * 2 * Math.PI
                        val r1 = 26f
                        val r2 = 40f
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(cx + (r1 * kotlin.math.cos(angle)).toFloat(), cy + (r1 * kotlin.math.sin(angle)).toFloat()),
                            end = Offset(cx + (r2 * kotlin.math.cos(angle)).toFloat(), cy + (r2 * kotlin.math.sin(angle)).toFloat()),
                            strokeWidth = 3f,
                        )
                    }
                }
                drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 20f, center = Offset(cx, cy))
            }
            WeatherMood.CLOUDY -> {
                val rng = Random(7)
                repeat(4) { i ->
                    val baseX = ((drift + i / 4f) % 1.3f) * size.width - size.width * 0.15f
                    val y = size.height * (0.2f + i * 0.15f)
                    drawCircle(color = Color.White.copy(alpha = 0.12f), radius = 46f, center = Offset(baseX, y))
                    drawCircle(color = Color.White.copy(alpha = 0.10f), radius = 32f, center = Offset(baseX + 34f, y + 8f))
                }
            }
            WeatherMood.NIGHT -> {
                val rng = Random(99)
                repeat(40) {
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height
                    drawCircle(color = Color.White.copy(alpha = rng.nextFloat() * 0.6f), radius = 1.4f, center = Offset(x, y))
                }
            }
        }
    }
}
