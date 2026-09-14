package com.ahrn.irrigatech.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ahrn.irrigatech.IrrigaTechApp
import com.ahrn.irrigatech.ui.viewmodel.AlertsViewModel
import com.ahrn.irrigatech.ui.viewmodel.AuthViewModel
import com.ahrn.irrigatech.ui.viewmodel.DashboardViewModel
import com.ahrn.irrigatech.ui.viewmodel.SettingsViewModel
import com.ahrn.irrigatech.ui.viewmodel.SetupViewModel

object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer { AuthViewModel(app().container.authRepository) }

        initializer {
            SetupViewModel(
                deviceStore = app().container.deviceStore,
                tokenStore = app().container.tokenStore,
                repository = app().container.sensorRepository,
            )
        }

        initializer {
            DashboardViewModel(
                deviceStore = app().container.deviceStore,
                tokenStore = app().container.tokenStore,
                settingsStore = app().container.settingsStore,
                alertRepository = app().container.alertRepository,
                repository = app().container.sensorRepository,
            )
        }

        initializer { AlertsViewModel(app().container.alertRepository) }

        initializer {
            SettingsViewModel(
                settingsStore = app().container.settingsStore,
                deviceStore = app().container.deviceStore,
                authRepository = app().container.authRepository,
            )
        }
    }
}

private fun CreationExtras.app(): IrrigaTechApp = this[APPLICATION_KEY] as IrrigaTechApp
