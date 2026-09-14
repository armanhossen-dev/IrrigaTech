package com.ahrn.irrigatech.data.repository

import com.ahrn.irrigatech.data.model.ConnectionResult
import com.ahrn.irrigatech.data.model.ConnectionStatus
import com.ahrn.irrigatech.data.model.DeviceConfig
import com.ahrn.irrigatech.data.model.MotorId
import com.ahrn.irrigatech.data.model.SensorSnapshot
import com.ahrn.irrigatech.data.remote.BlynkDataSource
import com.ahrn.irrigatech.data.remote.MockDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A single seam the UI depends on. Blynk is used whenever a token exists and the
 * hardware answers; otherwise the demo data source keeps the app usable.
 */
interface SensorRepository {
    val lastSnapshot: StateFlow<SensorSnapshot?>
    val usingMock: StateFlow<Boolean>

    suspend fun refresh(device: DeviceConfig, token: String?): Result<SensorSnapshot>

    suspend fun setMotor(
        device: DeviceConfig,
        token: String?,
        motor: MotorId,
        on: Boolean,
    ): Result<Unit>

    suspend fun testConnection(device: DeviceConfig, token: String): ConnectionResult
}

class DefaultSensorRepository(
    private val blynk: BlynkDataSource,
    private val mock: MockDataSource,
) : SensorRepository {

    private val _lastSnapshot = MutableStateFlow<SensorSnapshot?>(null)
    override val lastSnapshot: StateFlow<SensorSnapshot?> = _lastSnapshot.asStateFlow()

    private val _usingMock = MutableStateFlow(false)
    override val usingMock: StateFlow<Boolean> = _usingMock.asStateFlow()

    override suspend fun refresh(device: DeviceConfig, token: String?): Result<SensorSnapshot> {
        if (token.isNullOrBlank()) {
            return runDemo()
        }
        return try {
            val snapshot = blynk.fetchSnapshot(token)
            _usingMock.value = false
            _lastSnapshot.value = snapshot
            Result.success(snapshot)
        } catch (e: Exception) {
            // Hardware unreachable: fall back to demo values but surface the error
            // through the Result so the UI can show the offline banner.
            val fallback = mock.fetchSnapshot(token)
            _usingMock.value = true
            _lastSnapshot.value = fallback
            Result.failure(e)
        }
    }

    override suspend fun setMotor(
        device: DeviceConfig,
        token: String?,
        motor: MotorId,
        on: Boolean,
    ): Result<Unit> {
        return try {
            if (token.isNullOrBlank()) {
                mock.writeMotor(motor, on)
            } else {
                blynk.writeMotor(token, motor, on)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(
        device: DeviceConfig,
        token: String,
    ): ConnectionResult {
        if (token.isBlank()) {
            return ConnectionResult(ConnectionStatus.FAILED, "Enter the Blynk auth token first")
        }
        return blynk.testConnection(token)
    }

    private suspend fun runDemo(): Result<SensorSnapshot> {
        val snapshot = mock.fetchSnapshot(null)
        _usingMock.value = true
        _lastSnapshot.value = snapshot
        return Result.success(snapshot)
    }
}
