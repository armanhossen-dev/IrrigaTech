package com.ahrn.irrigatech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.data.auth.AuthRepository
import com.ahrn.irrigatech.data.local.AppSettings
import com.ahrn.irrigatech.data.local.DeviceConfigStore
import com.ahrn.irrigatech.data.local.SettingsStore
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.ui.theme.ThemeModeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val deviceStore: DeviceConfigStore,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsStore.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    val devices: StateFlow<List<DeviceConfig>> = deviceStore.devices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _signingOut = MutableStateFlow(false)
    val signingOut: StateFlow<Boolean> = _signingOut

    fun setTheme(mode: ThemeModeOption) = launch { settingsStore.setTheme(mode) }

    fun setAccent(id: String) = launch { settingsStore.setAccent(id) }

    fun setPollSeconds(seconds: Int) = launch { settingsStore.setPollSeconds(seconds) }

    fun setCelsius(value: Boolean) = launch { settingsStore.setCelsius(value) }

    fun setNotifyTankFull(value: Boolean) = launch { settingsStore.setNotifyTankFull(value) }

    fun setNotifyRain(value: Boolean) = launch { settingsStore.setNotifyRain(value) }

    fun setNotifyMotorAutoStop(value: Boolean) = launch { settingsStore.setNotifyMotorAutoStop(value) }

    fun setNotifyLowBattery(value: Boolean) = launch { settingsStore.setNotifyLowBattery(value) }

    fun activate(device: DeviceConfig) = launch { deviceStore.setActive(device.id) }

    fun signOut(onDone: () -> Unit) {
        if (_signingOut.value) return
        _signingOut.value = true
        viewModelScope.launch {
            authRepository.signOut()
            _signingOut.value = false
            onDone()
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
