package com.ahrn.irrigatech.ui.navigation

import com.ahrn.irrigatech.data.model.MotorId

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SETUP = "setup"
    const val HOME = "home"

    const val DASHBOARD = "dashboard"
    const val ALERTS = "alerts"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val FEEDBACK = "feedback"

    const val MOTOR = "motor/{motorId}"
    fun motor(motor: MotorId) = "motor/${motor.name}"
}
