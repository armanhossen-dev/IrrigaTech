import 'package:flutter/material.dart';

import '../../data/models/alert_event.dart';
import 'app_theme.dart';

class StatusVisuals {
  static const IconData battery = Icons.battery_std_outlined;
  static const IconData voltage = Icons.bolt_outlined;
  static const IconData temperature = Icons.thermostat_outlined;
  static const IconData moisture = Icons.grass_outlined;
  static const IconData rain = Icons.umbrella_outlined;
  static const IconData tank = Icons.water_outlined;
  static const IconData motor = Icons.power_settings_new;
  static const IconData forecast = Icons.wb_cloudy_outlined;
  static const IconData security = Icons.gpp_maybe_outlined;
  static const IconData boot = Icons.check_circle_outline;
  static const IconData report = Icons.article_outlined;
  static const IconData sensorError = Icons.sensors_off_outlined;
  static const IconData connection = Icons.cloud_off_outlined;
  static const IconData tankFull = Icons.water_damage_outlined;
  static const IconData tankEmpty = Icons.water_drop_outlined;

  static IconData iconFor(AlertType type) {
    return switch (type) {
      AlertType.report => report,
      AlertType.rain => rain,
      AlertType.tankEmpty => tankEmpty,
      AlertType.tankFull => tankFull,
      AlertType.motorAuto => motor,
      AlertType.boot => boot,
      AlertType.tamper => security,
      AlertType.lowBattery => battery,
      AlertType.sensorError => sensorError,
      AlertType.connection => connection,
    };
  }

  static Color colorForSeverity(AlertSeverity severity) {
    return switch (severity) {
      AlertSeverity.info => AppColors.info,
      AlertSeverity.success => AppColors.success,
      AlertSeverity.warning => AppColors.warning,
      AlertSeverity.critical => AppColors.danger,
      AlertSeverity.security => AppColors.danger,
    };
  }

  static Color colorFor(AlertType type, AlertSeverity severity) {
    if (type == AlertType.tamper) return AppColors.danger;
    return colorForSeverity(severity);
  }
}
