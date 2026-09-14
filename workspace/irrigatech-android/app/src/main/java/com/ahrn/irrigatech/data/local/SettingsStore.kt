package com.ahrn.irrigatech.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ahrn.irrigatech.ui.theme.AccentGreenBlue
import com.ahrn.irrigatech.ui.theme.AccentSwatches
import com.ahrn.irrigatech.ui.theme.ThemeModeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "irrigatech_settings")

data class AppSettings(
    val themeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    val accentId: String = AccentGreenBlue.id,
    val pollSeconds: Int = 7,
    val useCelsius: Boolean = true,
    val notifyTankFull: Boolean = true,
    val notifyRain: Boolean = true,
    val notifyMotorAutoStop: Boolean = true,
    val notifyLowBattery: Boolean = true,
)

class SettingsStore(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeModeOption.valueOf(it) }.getOrNull() }
                ?: ThemeModeOption.SYSTEM,
            accentId = prefs[KEY_ACCENT] ?: AccentGreenBlue.id,
            pollSeconds = prefs[KEY_POLL] ?: 7,
            useCelsius = prefs[KEY_CELSIUS] ?: true,
            notifyTankFull = prefs[KEY_NOTIFY_TANK] ?: true,
            notifyRain = prefs[KEY_NOTIFY_RAIN] ?: true,
            notifyMotorAutoStop = prefs[KEY_NOTIFY_MOTOR] ?: true,
            notifyLowBattery = prefs[KEY_NOTIFY_BATTERY] ?: true,
        )
    }

    suspend fun setTheme(mode: ThemeModeOption) = context.settingsDataStore.edit {
        it[KEY_THEME] = mode.name
    }

    suspend fun setAccent(accentId: String) = context.settingsDataStore.edit {
        require(AccentSwatches.any { swatch -> swatch.id == accentId }) { "Unknown accent" }
        it[KEY_ACCENT] = accentId
    }

    suspend fun setPollSeconds(seconds: Int) = context.settingsDataStore.edit {
        it[KEY_POLL] = seconds.coerceIn(5, 30)
    }

    suspend fun setCelsius(value: Boolean) = context.settingsDataStore.edit {
        it[KEY_CELSIUS] = value
    }

    suspend fun setNotifyTankFull(value: Boolean) = context.settingsDataStore.edit {
        it[KEY_NOTIFY_TANK] = value
    }

    suspend fun setNotifyRain(value: Boolean) = context.settingsDataStore.edit {
        it[KEY_NOTIFY_RAIN] = value
    }

    suspend fun setNotifyMotorAutoStop(value: Boolean) = context.settingsDataStore.edit {
        it[KEY_NOTIFY_MOTOR] = value
    }

    suspend fun setNotifyLowBattery(value: Boolean) = context.settingsDataStore.edit {
        it[KEY_NOTIFY_BATTERY] = value
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_ACCENT = stringPreferencesKey("accent_id")
        val KEY_POLL = intPreferencesKey("poll_seconds")
        val KEY_CELSIUS = booleanPreferencesKey("use_celsius")
        val KEY_NOTIFY_TANK = booleanPreferencesKey("notify_tank_full")
        val KEY_NOTIFY_RAIN = booleanPreferencesKey("notify_rain")
        val KEY_NOTIFY_MOTOR = booleanPreferencesKey("notify_motor_auto_stop")
        val KEY_NOTIFY_BATTERY = booleanPreferencesKey("notify_low_battery")
    }
}
