package com.ahrn.irrigatech.ui.util

import com.ahrn.irrigatech.data.model.SensorSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
private val dateTimeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

fun formatTemperature(celsius: Double?, useCelsius: Boolean): String {
    if (celsius == null) return "--"
    val value = if (useCelsius) celsius else celsius * 9.0 / 5.0 + 32.0
    return String.format(Locale.US, "%.1f", value)
}

fun temperatureUnit(useCelsius: Boolean): String = if (useCelsius) "\u00B0C" else "\u00B0F"

fun formatPercent(value: Double?): String =
    if (value == null) "--" else value.coerceIn(0.0, 100.0).toInt().toString()

fun formatVoltage(value: Double?): String =
    if (value == null) "--" else String.format(Locale.US, "%.1f", value)

fun formatClock(timestamp: Long?): String =
    if (timestamp == null) "--" else timeFormat.format(Date(timestamp))

fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

fun relativeTime(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - timestamp).coerceAtLeast(0)
    val seconds = diff / 1000
    return when {
        seconds < 10 -> "just now"
        seconds < 60 -> "${seconds}s ago"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86_400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86_400}d ago"
    }
}

fun SensorSnapshot.temperatureDisplay(useCelsius: Boolean): String =
    if (temperatureFault) "--" else formatTemperature(temperatureC, useCelsius)

fun SensorSnapshot.temperatureCaption(useCelsius: Boolean): String? = when {
    temperatureFault -> "Sensor error"
    temperatureC == null -> "No reading"
    temperatureC > 40 -> "Very hot"
    temperatureC < 10 -> "Cold"
    else -> null
}
