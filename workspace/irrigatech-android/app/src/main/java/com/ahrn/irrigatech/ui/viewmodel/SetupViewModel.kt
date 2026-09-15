package com.ahrn.irrigatech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.data.local.DeviceConfigStore
import com.ahrn.irrigatech.data.local.SecureTokenStore
import com.ahrn.irrigatech.data.model.ConnectionResult
import com.ahrn.irrigatech.data.model.ConnectionStatus
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.data.repository.SensorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SetupForm(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val token: String = "",
    val templateId: String = DeviceConfig.DEFAULT_TEMPLATE,
    val location: String = "",
    val showToken: Boolean = false,
) {
    val canSave: Boolean get() = name.isNotBlank() && token.isNotBlank()
}

sealed interface TestState {
    data object Idle : TestState
    data object Testing : TestState
    data class Done(val result: ConnectionResult) : TestState
}

class SetupViewModel(
    private val deviceStore: DeviceConfigStore,
    private val tokenStore: SecureTokenStore,
    private val repository: SensorRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(SetupForm())
    val form: StateFlow<SetupForm> = _form

    private val _testState = MutableStateFlow<TestState>(TestState.Idle)
    val testState: StateFlow<TestState> = _testState

    val devices: StateFlow<List<DeviceConfig>> = deviceStore.devices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun edit(transform: (SetupForm) -> SetupForm) = _form.update(transform)

    fun toggleTokenVisibility() = _form.update { it.copy(showToken = !it.showToken) }

    fun loadDevice(device: DeviceConfig) {
        viewModelScope.launch {
            _form.value = SetupForm(
                id = device.id,
                name = device.name,
                token = tokenStore.readToken(device.id).orEmpty(),
                templateId = device.templateId,
                location = device.location,
            )
        }
    }

    fun newDevice() {
        _form.value = SetupForm()
        _testState.value = TestState.Idle
    }

    fun testConnection() {
        val current = _form.value
        _testState.value = TestState.Testing
        viewModelScope.launch {
            val result = repository.testConnection(
                DeviceConfig(
                    id = current.id,
                    name = current.name.ifBlank { "Controller" },
                    templateId = current.templateId,
                    location = current.location,
                ),
                current.token.trim(),
            )
            _testState.value = TestState.Done(result)
        }
    }

    fun save(onSaved: (DeviceConfig) -> Unit) {
        val current = _form.value
        if (!current.canSave) return
        viewModelScope.launch {
            val device = DeviceConfig(
                id = current.id,
                name = current.name.trim(),
                templateId = current.templateId.trim().ifBlank { DeviceConfig.DEFAULT_TEMPLATE },
                location = current.location.trim(),
            )
            tokenStore.saveToken(device.id, current.token.trim())
            deviceStore.upsert(device)
            deviceStore.setActive(device.id)
            onSaved(device)
        }
    }

    fun deleteDevice(device: DeviceConfig) {
        viewModelScope.launch {
            tokenStore.deleteToken(device.id)
            deviceStore.remove(device.id)
            if (_form.value.id == device.id) _form.value = SetupForm()
        }
    }

    fun activate(device: DeviceConfig) {
        viewModelScope.launch { deviceStore.setActive(device.id) }
    }

    fun dismissTest() {
        _testState.value = TestState.Idle
    }
}
