package com.ahrn.irrigatech.data.remote

import com.ahrn.irrigatech.data.model.ConnectionResult
import com.ahrn.irrigatech.data.model.ConnectionStatus
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.SensorSnapshot
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

const val BLYNK_BASE_URL = "https://blynk.cloud/external/api/"

object BlynkPins {
    const val TEMPERATURE = "V0"
    const val MOISTURE = "V1"
    const val FIELD_MOTOR = "V2"
    const val TANK_MOTOR = "V3"
    const val BATTERY = "V4"
    const val VOLTAGE = "V5"

    val readable = listOf(TEMPERATURE, MOISTURE, FIELD_MOTOR, TANK_MOTOR, BATTERY, VOLTAGE)
}

/**
 * Talks to Blynk over REST. No official Flutter/Android SDK is used, so all
 * calls go through the documented external API endpoints.
 */
class BlynkDataSource(
    private val api: BlynkApi,
) {

    suspend fun fetchSnapshot(token: String): SensorSnapshot {
        // Blynk's read API expects bare pin names, e.g. .../get?token=XXX&V0&V1,
        // so the query is assembled as a string rather than via query parameters.
        val url = buildString {
            append(BLYNK_BASE_URL)
            append("get?token=")
            append(token)
            BlynkPins.readable.forEach { append('&').append(it) }
        }
        val json = api.get(url)
        return parse(json)
    }

    suspend fun writeMotor(token: String, motor: MotorId, on: Boolean) {
        val url = buildUrl("update", token) { builder ->
            builder.addQueryParameter(motor.pin, if (on) "1" else "0")
        }
        api.update(url)
    }

    suspend fun testConnection(token: String): ConnectionResult {
        return try {
            val url = "$BLYNK_BASE_URL" + "get?token=" + token + "&" + BlynkPins.TEMPERATURE
            api.get(url)
            ConnectionResult(ConnectionStatus.OK, "Connected to Blynk Cloud")
        } catch (e: IOException) {
            ConnectionResult(ConnectionStatus.FAILED, "Network error: ${e.message ?: "unreachable"}")
        } catch (e: Exception) {
            ConnectionResult(
                ConnectionStatus.FAILED,
                "Rejected. Check the auth token and template.",
            )
        }
    }

    private fun buildUrl(
        path: String,
        token: String,
        configure: (HttpUrl.Builder) -> Unit,
    ): String {
        val builder = (BLYNK_BASE_URL + path).toHttpUrlOrNull()?.newBuilder()
            ?: error("Invalid Blynk base URL")
        builder.addQueryParameter("token", token)
        configure(builder)
        return builder.build().toString()
    }

    private fun parse(json: JsonElement): SensorSnapshot {
        val values = flatten(json)
        val now = System.currentTimeMillis()
        return SensorSnapshot(
            temperatureC = values[BlynkPins.TEMPERATURE],
            moisturePercent = values[BlynkPins.MOISTURE],
            batteryPercent = values[BlynkPins.BATTERY],
            voltage = values[BlynkPins.VOLTAGE],
            rain = false,
            tankFull = false,
            fieldMotorOn = (values[BlynkPins.FIELD_MOTOR] ?: 0.0) != 0.0,
            tankMotorOn = (values[BlynkPins.TANK_MOTOR] ?: 0.0) != 0.0,
            timestamp = now,
        )
    }

    /**
     * Blynk may answer with an object keyed by pin, or a positional array when
     * multiple pins are requested. Normalize both into pin -> Double.
     */
    private fun flatten(json: JsonElement): Map<String, Double> {
        val out = mutableMapOf<String, Double>()
        when (json) {
            is JsonObject -> json.forEach { (key, value) ->
                value.firstNumber()?.let { out[key] = it }
            }

            is kotlinx.serialization.json.JsonArray -> {
                json.forEachIndexed { index, value ->
                    val pin = BlynkPins.readable.getOrNull(index) ?: return@forEachIndexed
                    value.firstNumber()?.let { out[pin] = it }
                }
            }

            else -> json.firstNumber()?.let { out[BlynkPins.TEMPERATURE] = it }
        }
        return out
    }

    private fun JsonElement.firstNumber(): Double? = when (this) {
        is JsonPrimitive -> {
            doubleOrNull ?: content.toDoubleOrNull() ?: booleanOrNull?.let { if (it) 1.0 else 0.0 }
        }
        is kotlinx.serialization.json.JsonArray -> firstOrNull()?.firstNumber()
        else -> null
    }
}
