package com.ahrn.irrigatech.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ahrn.irrigatech.data.model.AlertEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.alertDataStore by preferencesDataStore(name = "irrigatech_alerts")

/** Local chronological event log, capped so the store cannot grow unbounded. */
class AlertRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val alerts: Flow<List<AlertEvent>> = context.alertDataStore.data.map { prefs ->
        val raw = prefs[KEY_ALERTS] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<AlertEvent>>(raw) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.timestamp }
    }

    suspend fun add(event: AlertEvent) {
        context.alertDataStore.edit { prefs ->
            val current = prefs[KEY_ALERTS]
                ?.let { runCatching { json.decodeFromString<List<AlertEvent>>(it) }.getOrNull() }
                .orEmpty()
            val updated = (current + event)
                .distinctBy { it.id }
                .sortedByDescending { it.timestamp }
                .take(MAX_EVENTS)
            prefs[KEY_ALERTS] = json.encodeToString<List<AlertEvent>>(updated)
        }
    }

    suspend fun clear() {
        context.alertDataStore.edit { it.remove(KEY_ALERTS) }
    }

    /** Seeds a short, believable history the first time the app runs. */
    suspend fun seedIfEmpty() {
        context.alertDataStore.edit { prefs ->
            if (prefs[KEY_ALERTS] != null) return@edit
            val now = System.currentTimeMillis()
            val seeded = listOf(
                AlertEvent(
                    id = "seed-motor",
                    timestamp = now - 3 * 60 * 60 * 1000L,
                    kind = com.ahrn.irrigatech.data.model.AlertKind.MOTOR,
                    severity = com.ahrn.irrigatech.data.model.AlertSeverity.GOOD,
                    title = "Tank motor started",
                    detail = "Tank reported empty. Motor 2 started automatically.",
                ),
                AlertEvent(
                    id = "seed-rain",
                    timestamp = now - 6 * 60 * 60 * 1000L,
                    kind = com.ahrn.irrigatech.data.model.AlertKind.RAIN,
                    severity = com.ahrn.irrigatech.data.model.AlertSeverity.INFO,
                    title = "Rain started",
                    detail = "Field motor blocked and stopped to save water.",
                ),
                AlertEvent(
                    id = "seed-system",
                    timestamp = now - 8 * 60 * 60 * 1000L,
                    kind = com.ahrn.irrigatech.data.model.AlertKind.SYSTEM,
                    severity = com.ahrn.irrigatech.data.model.AlertSeverity.GOOD,
                    title = "Controller online",
                    detail = "Controller booted and is reporting telemetry.",
                ),
            )
            prefs[KEY_ALERTS] = json.encodeToString<List<AlertEvent>>(seeded)
        }
    }

    private companion object {
        val KEY_ALERTS = stringPreferencesKey("alerts_json")
        const val MAX_EVENTS = 300
    }
}
