class AppConstants {
  static const String appName = 'IrrigaTech';
  static const String tagline = 'Smart Irrigation. Effortless Farming.';
  static const String packageName = 'com.ahrn.irrigatech';
  static const String credit = 'Built by Arman Hossen (AHRN)';

  static const String blynkBaseUrl = 'https://blynk.cloud/external/api';
  static const String pinTemperature = 'V0';
  static const String pinMoisture = 'V1';
  static const String pinMotor1 = 'V2';
  static const String pinMotor2 = 'V3';
  static const String pinBattery = 'V4';
  static const String pinVoltage = 'V5';

  static const double temperatureErrorSentinel = -127.0;

  static const Duration defaultPollInterval = Duration(seconds: 3);
  static const Duration minPollInterval = Duration(seconds: 2);
  static const Duration maxPollInterval = Duration(seconds: 10);

  static const String hiveBoxHistory = 'sensor_history';
  static const String hiveBoxAlerts = 'alert_events';
  static const String hiveBoxSettings = 'app_settings';

  static const String prefsOnboardingDone = 'onboarding_done';
  static const String prefsThemeMode = 'theme_mode';
  static const String prefsUseDemo = 'use_demo_mode';
  static const String prefsPollSeconds = 'poll_seconds';
  static const String prefsUseCelsius = 'use_celsius';
  static const String prefsNotifyTank = 'notify_tank';
  static const String prefsNotifyRain = 'notify_rain';
  static const String prefsNotifyTamper = 'notify_tamper';
  static const String prefsNotifyBattery = 'notify_battery';
  static const String prefsDemoSignedIn = 'demo_signed_in';
  static const String secureBlynkToken = 'blynk_auth_token';

  static const double lowBatteryThreshold = 20.0;
  static const int tamperWrongCodeThreshold = 3;
}
