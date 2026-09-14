package com.ahrn.irrigatech.data.model

import kotlinx.serialization.Serializable

/**
 * A saved irrigation controller. The Blynk token is never stored here; it lives
 * in EncryptedSharedPreferences keyed by [id] (see SecureTokenStore).
 */
@Serializable
data class DeviceConfig(
    val id: String,
    val name: String,
    val templateId: String = DEFAULT_TEMPLATE,
    val location: String = "",
    val telegramBot: String = "",
) {
    companion object {
        const val DEFAULT_TEMPLATE = "TMPL6lSiWFFsO"
    }
}

enum class MotorId(val pin: String, val displayName: String) {
    FIELD("V2", "Field Motor"),
    TANK("V3", "Tank Motor"),
}

data class SensorSnapshot(
    val temperatureC: Double?,
    val moisturePercent: Double?,
    val batteryPercent: Double?,
    val voltage: Double?,
    val rain: Boolean,
    val tankFull: Boolean,
    val fieldMotorOn: Boolean,
    val tankMotorOn: Boolean,
    val timestamp: Long,
) {
    val temperatureFault: Boolean
        get() = temperatureC != null && temperatureC <= SENTINEL_LIMIT

    fun isOn(motor: MotorId): Boolean = when (motor) {
        MotorId.FIELD -> fieldMotorOn
        MotorId.TANK -> tankMotorOn
    }

    companion object {
        const val SENTINEL = -127.0
        const val SENTINEL_LIMIT = -50.0

        fun empty() = SensorSnapshot(
            temperatureC = null,
            moisturePercent = null,
            batteryPercent = null,
            voltage = null,
            rain = false,
            tankFull = false,
            fieldMotorOn = false,
            tankMotorOn = false,
            timestamp = System.currentTimeMillis(),
        )
    }
}

enum class AlertKind { MOTOR, RAIN, TANK, BATTERY, SECURITY, SYSTEM }

enum class AlertSeverity { INFO, GOOD, WARNING, ALERT }

@Serializable
data class AlertEvent(
    val id: String,
    val timestamp: Long,
    val kind: AlertKind,
    val severity: AlertSeverity,
    val title: String,
    val detail: String,
)

enum class ConnectionStatus { UNKNOWN, OK, FAILED }

data class ConnectionResult(
    val status: ConnectionStatus,
    val message: String,
)

/** Explains why a motor toggle is disabled, or null when it can be used. */
data class MotorLock(
    val motor: MotorId,
    val reason: String,
)
