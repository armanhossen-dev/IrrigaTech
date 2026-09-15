package com.ahrn.irrigatech.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahrn.irrigatech.data.local.DeviceConfigStore
import com.ahrn.irrigatech.data.local.SecureTokenStore
import com.ahrn.irrigatech.data.local.SessionStore
import com.ahrn.irrigatech.data.local.SettingsStore
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.MotorLock
import com.ahrn.irrigatech.data.model.SensorSnapshot
import com.ahrn.irrigatech.data.repository.AlertRepository
import com.ahrn.irrigatech.data.repository.AlertRules
import com.ahrn.irrigatech.data.repository.SensorRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

data class PendingMotorAction(
    val motor: MotorId,
    val turningOn: Boolean,
)

data class DashboardUiState(
    val device: DeviceConfig? = null,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val snapshot: SensorSnapshot? = null,
    val lastSyncedAt: Long? = null,
    val offline: Boolean = false,
    val usingMock: Boolean = false,
    val error: String? = null,
    val togglingMotor: MotorId? = null,
    val pendingAction: PendingMotorAction? = null,
    val celsius: Boolean = true,
    val keypadLocked: Boolean = true,
    val keypadError: String? = null,
    val userPhotoUrl: String? = null,
    val userDisplayName: String? = null,
) {
    fun lockFor(motor: MotorId): MotorLock? {
        val current = snapshot ?: return null
        return when {
            motor == MotorId.FIELD && current.rain ->
                MotorLock(motor, "Blocked while it is raining")

            motor == MotorId.TANK && current.tankFull ->
                MotorLock(motor, "Blocked while the tank is full")

            else -> null
        }
    }
}

class DashboardViewModel(
    private val deviceStore: DeviceConfigStore,
    private val tokenStore: SecureTokenStore,
    private val settingsStore: SettingsStore,
    private val sessionStore: SessionStore,
    private val alertRepository: AlertRepository,
    private val repository: SensorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var token: String? = null

    init {
        viewModelScope.launch {
            deviceStore.activeDevice()?.let { device ->
                token = tokenStore.readToken(device.id)
                _uiState.update { it.copy(device = device) }
            }
            settingsStore.settings.collect { settings ->
                _uiState.update { it.copy(celsius = settings.useCelsius) }
            }
        }
        viewModelScope.launch {
            sessionStore.user.collect { user ->
                _uiState.update { it.copy(userPhotoUrl = user?.photoUrl, userDisplayName = user?.displayName) }
            }
        }
    }

    /**
     * Polls the controller until the coroutine is cancelled. The dashboard calls
     * this inside repeatOnLifecycle(STARTED) so polling pauses in the background.
     */
    suspend fun poll() {
        while (coroutineContext.isActive) {
            fetch(silent = _uiState.value.snapshot != null)
            val seconds = settingsStore.settings.first().pollSeconds.coerceIn(1, 30)
            delay(seconds * 1000L)
        }
    }

    fun refresh() {
        viewModelScope.launch { fetch(silent = _uiState.value.snapshot != null, userInitiated = true) }
    }

    private suspend fun fetch(silent: Boolean, userInitiated: Boolean = false) {
        val device = _uiState.value.device ?: resolveDevice() ?: run {
            _uiState.update { it.copy(loading = false, error = "No controller configured") }
            return
        }
        if (!silent && !userInitiated) {
            _uiState.update { it.copy(loading = it.snapshot == null) }
        }
        if (userInitiated) _uiState.update { it.copy(refreshing = true) }

        val previous = _uiState.value.snapshot
        val result = repository.refresh(device, token)

        result.fold(
            onSuccess = { snapshot ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        snapshot = snapshot,
                        lastSyncedAt = snapshot.timestamp,
                        offline = false,
                        usingMock = repository.usingMock.value,
                        error = null,
                    )
                }
                recordAlerts(previous, snapshot)
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        snapshot = repository.lastSnapshot.value ?: it.snapshot,
                        lastSyncedAt = repository.lastSnapshot.value?.timestamp ?: it.lastSyncedAt,
                        offline = true,
                        usingMock = true,
                        error = error.message ?: "Controller unreachable",
                    )
                }
            },
        )
    }

    private suspend fun resolveDevice(): DeviceConfig? {
        val device = deviceStore.activeDevice() ?: return null
        token = tokenStore.readToken(device.id)
        _uiState.update { it.copy(device = device) }
        return device
    }

    private suspend fun recordAlerts(previous: SensorSnapshot?, current: SensorSnapshot) {
        val generated = AlertRules.evaluate(previous, current)
        if (generated.isEmpty()) return
        val existing = alertRepository.alerts.first()
        generated.forEach { alert ->
            if (!AlertRules.isDuplicate(existing, alert)) {
                alertRepository.add(AlertRules.toEvent(alert))
            }
        }
    }

    fun requestToggle(motor: MotorId, bypassSafety: Boolean = false) {
        val state = _uiState.value
        val snapshot = state.snapshot ?: return
        val turningOn = !snapshot.isOn(motor)

        // Show a temporary warning if safety conditions are met, but don't block.
        val lock = state.lockFor(motor)
        if (turningOn && lock != null) {
            _uiState.update { it.copy(error = "Manual Override: ${lock.reason}") }
        }

        applyToggle(motor, turningOn)
    }

    fun confirmPending() {
        val pending = _uiState.value.pendingAction ?: return
        _uiState.update { it.copy(pendingAction = null) }
        applyToggle(pending.motor, on = true)
    }

    fun dismissPending() {
        _uiState.update { it.copy(pendingAction = null, keypadError = null) }
    }

    fun stopMotor(motor: MotorId) {
        applyToggle(motor, on = false)
    }

    fun verifyKeypadPin(pin: String) {
        if (pin == "1234") { // Default security PIN
            val pending = _uiState.value.pendingAction
            _uiState.update { it.copy(keypadLocked = false, keypadError = null, pendingAction = null) }
            if (pending != null) {
                applyToggle(pending.motor, on = true)
            }
        } else {
            _uiState.update { it.copy(keypadError = "Incorrect PIN. Try again.") }
        }
    }

    fun lockKeypad() {
        _uiState.update { it.copy(keypadLocked = true) }
    }

    private fun applyToggle(motor: MotorId, on: Boolean) {
        val device = _uiState.value.device ?: return
        _uiState.update { it.copy(togglingMotor = motor) }
        viewModelScope.launch {
            // Ensure we have the latest token before writing
            val currentToken = tokenStore.readToken(device.id)
            repository.setMotor(device, currentToken, motor, on).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(togglingMotor = null, error = null)
                    }
                    // Immediate fetch to confirm change
                    fetch(silent = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            togglingMotor = null,
                            error = error.message ?: "Command failed",
                        )
                    }
                },
            )
        }
    }

    fun consumeError() = _uiState.update { it.copy(error = null) }
}
