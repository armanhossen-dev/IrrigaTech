import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/constants/app_constants.dart';
import '../data/services/secure_token_store.dart';

class AppSettings {
  const AppSettings({
    required this.themeMode,
    required this.useDemoMode,
    required this.pollInterval,
    required this.useCelsius,
    required this.notifyTank,
    required this.notifyRain,
    required this.notifyTamper,
    required this.notifyBattery,
    required this.onboardingDone,
    required this.hasBlynkToken,
    required this.demoSignedIn,
  });

  final ThemeMode themeMode;
  final bool useDemoMode;
  final Duration pollInterval;
  final bool useCelsius;
  final bool notifyTank;
  final bool notifyRain;
  final bool notifyTamper;
  final bool notifyBattery;
  final bool onboardingDone;
  final bool hasBlynkToken;
  final bool demoSignedIn;

  AppSettings copyWith({
    ThemeMode? themeMode,
    bool? useDemoMode,
    Duration? pollInterval,
    bool? useCelsius,
    bool? notifyTank,
    bool? notifyRain,
    bool? notifyTamper,
    bool? notifyBattery,
    bool? onboardingDone,
    bool? hasBlynkToken,
    bool? demoSignedIn,
  }) {
    return AppSettings(
      themeMode: themeMode ?? this.themeMode,
      useDemoMode: useDemoMode ?? this.useDemoMode,
      pollInterval: pollInterval ?? this.pollInterval,
      useCelsius: useCelsius ?? this.useCelsius,
      notifyTank: notifyTank ?? this.notifyTank,
      notifyRain: notifyRain ?? this.notifyRain,
      notifyTamper: notifyTamper ?? this.notifyTamper,
      notifyBattery: notifyBattery ?? this.notifyBattery,
      onboardingDone: onboardingDone ?? this.onboardingDone,
      hasBlynkToken: hasBlynkToken ?? this.hasBlynkToken,
      demoSignedIn: demoSignedIn ?? this.demoSignedIn,
    );
  }

  static AppSettings defaults() => const AppSettings(
        themeMode: ThemeMode.system,
        useDemoMode: true,
        pollInterval: AppConstants.defaultPollInterval,
        useCelsius: true,
        notifyTank: true,
        notifyRain: true,
        notifyTamper: true,
        notifyBattery: true,
        onboardingDone: false,
        hasBlynkToken: false,
        demoSignedIn: false,
      );
}

class SettingsController extends StateNotifier<AppSettings> {
  SettingsController(this._prefs, this._tokens)
      : super(AppSettings.defaults());

  final SharedPreferences _prefs;
  final SecureTokenStore _tokens;

  Future<void> load() async {
    final theme = _prefs.getString(AppConstants.prefsThemeMode) ?? 'system';
    final token = await _tokens.readToken();
    state = AppSettings(
      themeMode: switch (theme) {
        'light' => ThemeMode.light,
        'dark' => ThemeMode.dark,
        _ => ThemeMode.system,
      },
      useDemoMode: _prefs.getBool(AppConstants.prefsUseDemo) ?? true,
      pollInterval: Duration(
        seconds: _prefs.getInt(AppConstants.prefsPollSeconds) ?? 3,
      ),
      useCelsius: _prefs.getBool(AppConstants.prefsUseCelsius) ?? true,
      notifyTank: _prefs.getBool(AppConstants.prefsNotifyTank) ?? true,
      notifyRain: _prefs.getBool(AppConstants.prefsNotifyRain) ?? true,
      notifyTamper: _prefs.getBool(AppConstants.prefsNotifyTamper) ?? true,
      notifyBattery: _prefs.getBool(AppConstants.prefsNotifyBattery) ?? true,
      onboardingDone: _prefs.getBool(AppConstants.prefsOnboardingDone) ?? false,
      hasBlynkToken: token != null && token.isNotEmpty,
      demoSignedIn: _prefs.getBool(AppConstants.prefsDemoSignedIn) ?? false,
    );
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    await _prefs.setString(AppConstants.prefsThemeMode, mode.name);
    state = state.copyWith(themeMode: mode);
  }

  Future<void> setDemoMode(bool value) async {
    await _prefs.setBool(AppConstants.prefsUseDemo, value);
    state = state.copyWith(useDemoMode: value);
  }

  Future<void> setPollSeconds(int seconds) async {
    final clamped = seconds.clamp(2, 10);
    await _prefs.setInt(AppConstants.prefsPollSeconds, clamped);
    state = state.copyWith(pollInterval: Duration(seconds: clamped));
  }

  Future<void> setCelsius(bool value) async {
    await _prefs.setBool(AppConstants.prefsUseCelsius, value);
    state = state.copyWith(useCelsius: value);
  }

  Future<void> setNotify({
    bool? tank,
    bool? rain,
    bool? tamper,
    bool? battery,
  }) async {
    if (tank != null) {
      await _prefs.setBool(AppConstants.prefsNotifyTank, tank);
    }
    if (rain != null) {
      await _prefs.setBool(AppConstants.prefsNotifyRain, rain);
    }
    if (tamper != null) {
      await _prefs.setBool(AppConstants.prefsNotifyTamper, tamper);
    }
    if (battery != null) {
      await _prefs.setBool(AppConstants.prefsNotifyBattery, battery);
    }
    state = state.copyWith(
      notifyTank: tank ?? state.notifyTank,
      notifyRain: rain ?? state.notifyRain,
      notifyTamper: tamper ?? state.notifyTamper,
      notifyBattery: battery ?? state.notifyBattery,
    );
  }

  Future<void> completeOnboarding() async {
    await _prefs.setBool(AppConstants.prefsOnboardingDone, true);
    state = state.copyWith(onboardingDone: true);
  }

  Future<void> saveToken(String token) async {
    await _tokens.saveToken(token.trim());
    state = state.copyWith(hasBlynkToken: token.trim().isNotEmpty);
  }

  Future<void> clearToken() async {
    await _tokens.clearToken();
    state = state.copyWith(hasBlynkToken: false);
  }

  Future<String?> readToken() => _tokens.readToken();

  Future<void> setDemoSignedIn(bool value) async {
    await _prefs.setBool(AppConstants.prefsDemoSignedIn, value);
    state = state.copyWith(demoSignedIn: value);
  }
}

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw StateError('SharedPreferences must be overridden in main()');
});

final secureTokenStoreProvider = Provider<SecureTokenStore>((ref) {
  return SecureTokenStore();
});

final settingsControllerProvider =
    StateNotifierProvider<SettingsController, AppSettings>((ref) {
  return SettingsController(
    ref.watch(sharedPreferencesProvider),
    ref.watch(secureTokenStoreProvider),
  );
});
