package com.ahrn.irrigatech.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ahrn.irrigatech.data.model.DeviceConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.deviceDataStore by preferencesDataStore(name = "irrigatech_devices")

/** Persists saved devices and the active device id as JSON in DataStore. */
class DeviceConfigStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val devices: Flow<List<DeviceConfig>> = context.deviceDataStore.data.map { prefs ->
        val raw = prefs[KEY_DEVICES] ?: return@map emptyList()
        runCatching {
            json.decodeFromString<List<DeviceConfig>>(raw)
        }.getOrDefault(emptyList())
    }

    val activeDeviceId: Flow<String?> = context.deviceDataStore.data.map { it[KEY_ACTIVE] }

    suspend fun currentDevices(): List<DeviceConfig> = devices.first()

    suspend fun activeDevice(): DeviceConfig? {
        val all = currentDevices()
        val activeId = activeDeviceId.first()
        return all.firstOrNull { it.id == activeId } ?: all.firstOrNull()
    }

    suspend fun upsert(config: DeviceConfig) {
        context.deviceDataStore.edit { prefs ->
            val all = prefs[KEY_DEVICES]
                ?.let { runCatching { json.decodeFromString<List<DeviceConfig>>(it) }.getOrNull() }
                .orEmpty()
            val updated = all.filterNot { it.id == config.id } + config
            prefs[KEY_DEVICES] = json.encodeToString<List<DeviceConfig>>(updated)
            if (prefs[KEY_ACTIVE].isNullOrBlank()) {
                prefs[KEY_ACTIVE] = config.id
            }
        }
    }

    suspend fun remove(deviceId: String) {
        context.deviceDataStore.edit { prefs ->
            val all = prefs[KEY_DEVICES]
                ?.let { runCatching { json.decodeFromString<List<DeviceConfig>>(it) }.getOrNull() }
                .orEmpty()
            prefs[KEY_DEVICES] = json.encodeToString<List<DeviceConfig>>(all.filterNot { it.id == deviceId })
            if (prefs[KEY_ACTIVE] == deviceId) {
                val nextId = all.firstOrNull { it.id != deviceId }?.id
                if (nextId != null) {
                    prefs[KEY_ACTIVE] = nextId
                } else {
                    prefs.remove(KEY_ACTIVE)
                }
            }
        }
    }

    suspend fun setActive(deviceId: String) {
        context.deviceDataStore.edit { it[KEY_ACTIVE] = deviceId }
    }

    private companion object {
        val KEY_DEVICES = stringPreferencesKey("devices_json")
        val KEY_ACTIVE = stringPreferencesKey("active_device_id")
    }
}
