package com.ahrn.irrigatech

import android.app.Application
import com.ahrn.irrigatech.di.AppContainer
import com.ahrn.irrigatech.notifications.AlertCheckWorker
import com.ahrn.irrigatech.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IrrigaTechApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
        AlertCheckWorker.schedule(this)

        // Seed a short demo history on first launch so the alerts screen is not
        // empty. Real events are appended on top of this.
        appScope.launch { container.alertRepository.seedIfEmpty() }
    }
}
