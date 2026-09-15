package com.ahrn.irrigatech.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.data.repository.AlertRules
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Periodic safety sweep. Runs even when the app is closed so a dry field, low
 * battery, or tank-full condition still reaches the farmer.
 */
class AlertCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? IrrigaTechApp)?.container ?: return Result.success()

        val device = container.deviceStore.activeDevice() ?: return Result.success()
        val token = container.tokenStore.readToken(device.id)
        val settings = container.settingsStore.settings.first()

        val result = container.sensorRepository.refresh(device, token)
        val snapshot = result.getOrNull() ?: container.sensorRepository.lastSnapshot.value
            ?: return Result.success()

        val generated = AlertRules.evaluate(previous = null, current = snapshot)
        generated.forEach { alert ->
            if (!shouldNotify(alert.kind.name, settings)) return@forEach
            val event = AlertRules.toEvent(alert)
            if (!AlertRules.isDuplicate(container.alertRepository.alerts.first(), alert)) {
                container.alertRepository.add(event)
                NotificationHelper.notify(
                    applicationContext,
                    event.id.hashCode(),
                    event.title,
                    event.detail,
                )
            }
        }
        return Result.success()
    }

    private fun shouldNotify(
        kind: String,
        settings: com.ahrn.irrigatech.data.local.AppSettings,
    ): Boolean = when (kind) {
        "TANK" -> settings.notifyTankFull
        "RAIN" -> settings.notifyRain
        "MOTOR" -> settings.notifyMotorAutoStop
        "BATTERY" -> settings.notifyLowBattery
        else -> true
    }

    companion object {
        private const val WORK_NAME = "irrigatech_alert_check"

        fun schedule(context: Context, minutes: Long = 15) {
            val request = PeriodicWorkRequestBuilder<AlertCheckWorker>(
                minutes.coerceAtLeast(15),
                TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
