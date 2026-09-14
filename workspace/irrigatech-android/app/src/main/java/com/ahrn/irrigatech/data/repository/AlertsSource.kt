package com.ahrn.irrigatech.data.repository

import com.ahrn.irrigatech.data.model.AlertEvent
import com.ahrn.irrigatech.data.model.AlertKind
import com.ahrn.irrigatech.data.model.AlertSeverity
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.SensorSnapshot
import java.util.UUID
import kotlin.math.abs

/**
 * Turns raw telemetry into meaningful alert events. Pure functions keep this
 * testable and independent of storage.
 */
object AlertRules {

    data class Generated(
        val kind: AlertKind,
        val severity: AlertSeverity,
        val title: String,
        val detail: String,
    )

    const val BATTERY_LOW = 20.0
    const val MOISTURE_DRY = 25.0

    fun evaluate(previous: SensorSnapshot?, current: SensorSnapshot): List<Generated> {
        val out = mutableListOf<Generated>()

        if (current.temperatureFault) {
            out += Generated(
                kind = AlertKind.SYSTEM,
                severity = AlertSeverity.WARNING,
                title = "Temperature sensor error",
                detail = "The temperature probe reported an invalid reading.",
            )
        }

        if (current.rain) {
            out += Generated(
                kind = AlertKind.RAIN,
                severity = AlertSeverity.INFO,
                title = "Rain detected",
                detail = "Field motor is blocked while it is raining to save water.",
            )
        }

        if (current.tankFull) {
            out += Generated(
                kind = AlertKind.TANK,
                severity = AlertSeverity.GOOD,
                title = "Tank full",
                detail = "Tank motor stopped automatically because the tank is full.",
            )
        }

        val battery = current.batteryPercent
        if (battery != null && battery <= BATTERY_LOW && (previous?.batteryPercent ?: 100.0) > BATTERY_LOW) {
            out += Generated(
                kind = AlertKind.BATTERY,
                severity = AlertSeverity.WARNING,
                title = "Battery low",
                detail = "Battery at ${battery.toInt()}%. Recharge the controller soon.",
            )
        }

        if (previous != null) {
            MotorId.entries.forEach { motor ->
                val was = previous.isOn(motor)
                val now = current.isOn(motor)
                if (was != now) {
                    out += Generated(
                        kind = AlertKind.MOTOR,
                        severity = if (now) AlertSeverity.GOOD else AlertSeverity.INFO,
                        title = "${motor.displayName} ${if (now) "started" else "stopped"}",
                        detail = if (now) {
                            "Motor switched on."
                        } else {
                            "Motor switched off."
                        },
                    )
                }
            }
        }

        val moisture = current.moisturePercent
        if (moisture != null && moisture <= MOISTURE_DRY) {
            out += Generated(
                kind = AlertKind.SYSTEM,
                severity = AlertSeverity.WARNING,
                title = "Soil is dry",
                detail = "Moisture at ${moisture.toInt()}%. Consider irrigating the field.",
            )
        }

        return out
    }

    fun toEvent(generated: Generated, now: Long = System.currentTimeMillis()) = AlertEvent(
        id = UUID.randomUUID().toString(),
        timestamp = now,
        kind = generated.kind,
        severity = generated.severity,
        title = generated.title,
        detail = generated.detail,
    )

    /** De-duplicates repetitive alerts so the log stays readable. */
    fun isDuplicate(existing: List<AlertEvent>, generated: Generated): Boolean {
        val latest = existing.firstOrNull { it.kind == generated.kind } ?: return false
        return latest.title == generated.title &&
            abs(System.currentTimeMillis() - latest.timestamp) < DUPLICATE_WINDOW_MS
    }

    private const val DUPLICATE_WINDOW_MS = 5 * 60 * 1000L
}
