package com.ahrn.irrigatech.data.remote

import com.ahrn.irrigatech.data.model.ConnectionResult
import com.ahrn.irrigatech.data.model.ConnectionStatus
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.SensorSnapshot
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Realistic fallback used when no Blynk token is configured or the device is
 * unreachable. Keeps every screen fully demonstrable without hardware.
 */
class MockDataSource {

    private var tick = 0
    private var temperature = 31.2
    private var moisture = 42.0
    private var battery = 100.0
    private var voltage = 36.3
    private var rain = false
    private var tankFull = false
    private var fieldMotor = false
    private var tankMotor = true

    suspend fun fetchSnapshot(token: String?): SensorSnapshot {
        delay(180)
        tick++
        temperature = (temperature + noise(0.4)).coerceIn(18.0, 42.0)
        moisture = (moisture + noise(1.6)).coerceIn(0.0, 100.0)
        battery = (battery - 0.01).coerceIn(12.0, 100.0)
        voltage = (voltage + noise(0.08)).coerceIn(32.0, 38.5)

        if (tick % 40 == 12) {
            rain = true
            fieldMotor = false
        }
        if (tick % 40 == 22) rain = false

        if (tankMotor && !tankFull && tick % 18 == 0) {
            tankFull = true
            tankMotor = false
        } else if (tankFull && tick % 25 == 5) {
            tankFull = false
            tankMotor = true
        }

        val sentinel = tick % 80 == 33
        return SensorSnapshot(
            temperatureC = if (sentinel) SensorSnapshot.SENTINEL else temperature,
            moisturePercent = moisture,
            batteryPercent = battery,
            voltage = voltage,
            rain = rain,
            tankFull = tankFull,
            fieldMotorOn = fieldMotor,
            tankMotorOn = tankMotor,
            timestamp = System.currentTimeMillis(),
        )
    }

    fun writeMotor(motor: MotorId, on: Boolean) {
        when (motor) {
            MotorId.FIELD -> if (!(rain && on)) fieldMotor = on
            MotorId.TANK -> if (!(tankFull && on)) tankMotor = on
        }
    }

    suspend fun testConnection(): ConnectionResult {
        delay(600)
        return ConnectionResult(ConnectionStatus.OK, "Demo controller reachable")
    }

    private fun noise(amplitude: Double): Double =
        (Random.nextDouble() * 2 - 1) * amplitude
}
