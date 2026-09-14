import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:uuid/uuid.dart';

import '../core/constants/app_constants.dart';
import '../core/utils/formatters.dart';
import '../data/local/local_store.dart';
import '../data/models/alert_event.dart';
import '../data/models/device_status.dart';
import '../data/models/sensor_reading.dart';
import '../data/repositories/blynk_repository.dart';
import '../data/repositories/demo_data_repository.dart';
import '../data/repositories/irrigation_repository.dart';
import '../data/services/auth_service.dart';
import '../data/services/notification_service.dart';
import 'auth_controller.dart';
import 'settings_controller.dart';

class DeviceViewState {
  const DeviceViewState({
    required this.status,
    required this.history,
    required this.alerts,
    required this.motorHistory,
    required this.foreground,
    required this.usingDemo,
  });

  final DeviceStatus status;
  final List<SensorReading> history;
  final List<AlertEvent> alerts;
  final List<MotorSnapshot> motorHistory;
  final bool foreground;
  final bool usingDemo;

  DeviceViewState copyWith({
    DeviceStatus? status,
    List<SensorReading>? history,
    List<AlertEvent>? alerts,
    List<MotorSnapshot>? motorHistory,
    bool? foreground,
    bool? usingDemo,
  }) {
    return DeviceViewState(
      status: status ?? this.status,
      history: history ?? this.history,
      alerts: alerts ?? this.alerts,
      motorHistory: motorHistory ?? this.motorHistory,
      foreground: foreground ?? this.foreground,
      usingDemo: usingDemo ?? this.usingDemo,
    );
  }
}

class DeviceController extends StateNotifier<DeviceViewState> {
  DeviceController({
    required this.settings,
    required this.auth,
    required this.store,
    required this.notifications,
  }) : super(
          DeviceViewState(
            status: DeviceStatus.demoSeed(),
            history: const [],
            alerts: const [],
            motorHistory: const [],
            foreground: true,
            usingDemo: true,
          ),
        );

  final SettingsController settings;
  final AuthService auth;
  final LocalStore store;
  final NotificationService notifications;

  IrrigationRepository? _repo;
  StreamSubscription<DeviceStatus>? _sub;
  StreamSubscription<List<ConnectivityResult>>? _netSub;
  DeviceStatus? _prev;
  bool _bootAlerted = false;
  final _uuid = const Uuid();

  Future<void> start() async {
    final alerts = store.alerts();
    var history = store.readingsSince(
      DateTime.now().subtract(const Duration(days: 30)),
    );
    if (history.length < 12) {
      history = _seedHistory();
      for (final r in history) {
        await store.addReading(r);
      }
    }
    final motors = await store.motorHistory();
    var seededAlerts = alerts;
    if (seededAlerts.isEmpty) {
      seededAlerts = _seedAlerts();
      for (final a in seededAlerts) {
        await store.addAlert(a);
      }
    }
    state = state.copyWith(
      alerts: seededAlerts,
      history: history,
      motorHistory: motors,
    );
    _bootAlerted = seededAlerts.any((a) => a.type == AlertType.boot);
    _netSub?.cancel();
    _netSub = Connectivity().onConnectivityChanged.listen((results) {
      final online = results.any((r) => r != ConnectivityResult.none);
      if (!online) {
        state = state.copyWith(
          status: state.status.copyWith(
            connected: false,
            lastError: 'No network connection. Showing last known values.',
          ),
        );
      }
    });
    await _bindRepository();
  }

  Future<void> _bindRepository() async {
    await _sub?.cancel();
    _repo?.dispose();
    final useDemo = settings.state.useDemoMode;
    if (useDemo) {
      _repo = DemoDataRepository();
    } else {
      final token = await settings.readToken();
      if (token == null || token.isEmpty) {
        _repo = DemoDataRepository();
      } else {
        _repo = BlynkRepository(token: token);
      }
    }
    state = state.copyWith(usingDemo: useDemo || _repo is DemoDataRepository);
    if (!state.foreground) return;
    _sub = _repo!
        .watchStatus(pollInterval: settings.state.pollInterval)
        .listen(_onStatus);
  }

  Future<void> rebuildSource() => _bindRepository();

  void setForeground(bool value) {
    state = state.copyWith(foreground: value);
    if (value) {
      _bindRepository();
    } else {
      _sub?.cancel();
      _repo?.dispose();
    }
  }

  Future<void> refresh() async {
    final status = await _repo?.fetchOnce();
    if (status != null) _onStatus(status);
  }

  Future<bool> testToken(String token) {
    return BlynkRepository(token: token).testConnection();
  }

  Future<void> toggleMotor(int index, bool on) async {
    final status = state.status;
    if (index == 1 && status.motor1Locked && on) return;
    if (index == 2 && status.motor2Locked && on) return;
    final next = await _repo?.setMotor(motorIndex: index, on: on);
    if (next != null) _onStatus(next);
  }

  void _onStatus(DeviceStatus status) {
    final prev = _prev;
    _prev = status;
    final history = [...state.history, status.reading];
    final trimmed = history.length > 4000
        ? history.sublist(history.length - 4000)
        : history;
    store.addReading(status.reading);
    auth.persistReading(status.reading.toMap());

    var alerts = List<AlertEvent>.from(state.alerts);
    var motors = List<MotorSnapshot>.from(state.motorHistory);

    if (!_bootAlerted) {
      _bootAlerted = true;
      alerts = _push(
        alerts,
        type: AlertType.boot,
        severity: AlertSeverity.success,
        icon: '✅',
        title: 'System online',
        body: '✅ System is online and booted successfully!',
      );
    }

    if (prev != null) {
      if (!prev.raining && status.raining) {
        alerts = _push(
          alerts,
          type: AlertType.rain,
          severity: AlertSeverity.warning,
          icon: '🌧️',
          title: 'Rain detected',
          body:
              '🌧️ Rain started. Motor 1 (field pump) locked and turned off automatically.',
        );
        if (settings.state.notifyRain) {
          notifications.show(
            id: 11,
            title: 'Rain detected',
            body: 'Field pump locked while raining.',
          );
        }
      }
      if (!prev.tankFull && status.tankFull) {
        alerts = _push(
          alerts,
          type: AlertType.tankFull,
          severity: AlertSeverity.warning,
          icon: '🛑',
          title: 'Tank full',
          body:
              '🚰 Tank is full! Motor 2 stopped automatically. Buzzer/alert fired on the device.',
        );
        if (settings.state.notifyTank) {
          notifications.show(
            id: 12,
            title: 'Tank full',
            body: 'Tank motor stopped automatically.',
          );
        }
      }
      if (prev.tankFull && !status.tankFull) {
        alerts = _push(
          alerts,
          type: AlertType.tankEmpty,
          severity: AlertSeverity.success,
          icon: '🚰',
          title: 'Tank empty',
          body: '🚰 Tank is empty! Motor 2 started automatically. ✅',
        );
      }
      if (prev.motor1On != status.motor1On || prev.motor2On != status.motor2On) {
        final snap = MotorSnapshot(
          motor1On: status.motor1On,
          motor2On: status.motor2On,
          changedAt: status.lastUpdated,
        );
        motors = [...motors, snap];
        store.addMotorSnapshot(snap);
      }
      if (!prev.tamperAlert && status.tamperAlert) {
        alerts = _push(
          alerts,
          type: AlertType.tamper,
          severity: AlertSeverity.security,
          icon: '🚨',
          title: 'Security / tamper alert',
          body:
              '🚨 3+ wrong keypad codes on the physical device. Access attempt blocked.',
        );
        if (settings.state.notifyTamper) {
          notifications.show(
            id: 13,
            title: 'Tamper alert',
            body: '3+ wrong keypad codes detected.',
          );
        }
      }
      if (status.reading.batteryPercent <= AppConstants.lowBatteryThreshold &&
          prev.reading.batteryPercent > AppConstants.lowBatteryThreshold) {
        alerts = _push(
          alerts,
          type: AlertType.lowBattery,
          severity: AlertSeverity.warning,
          icon: '🔋',
          title: 'Low battery',
          body:
              '🔋 Battery charge dropped to ${Formatters.percent(status.reading.batteryPercent)}.',
        );
        if (settings.state.notifyBattery) {
          notifications.show(
            id: 14,
            title: 'Low battery',
            body: Formatters.percent(status.reading.batteryPercent),
          );
        }
      }
      if (status.reading.temperatureFault && !prev.reading.temperatureFault) {
        alerts = _push(
          alerts,
          type: AlertType.sensorError,
          severity: AlertSeverity.warning,
          icon: '🌡️',
          title: 'Sensor Error',
          body:
              'Temperature probe returned the firmware sentinel (-127.0). Check wiring.',
        );
      }
      if (prev.connected && !status.connected) {
        alerts = _push(
          alerts,
          type: AlertType.connection,
          severity: AlertSeverity.warning,
          icon: '⚠️',
          title: 'Blynk unreachable',
          body: 'Blynk API is unreachable. Showing last known values.',
        );
      }
    }

    state = state.copyWith(
      status: status,
      history: trimmed,
      alerts: alerts,
      motorHistory: motors,
    );
  }

  List<AlertEvent> _push(
    List<AlertEvent> current, {
    required AlertType type,
    required AlertSeverity severity,
    required String icon,
    required String title,
    required String body,
  }) {
    final event = AlertEvent(
      id: _uuid.v4(),
      timestamp: DateTime.now(),
      type: type,
      severity: severity,
      title: title,
      body: body,
      icon: icon,
    );
    store.addAlert(event);
    auth.persistAlert(event.toMap());
    return [event, ...current];
  }

  List<AlertEvent> _seedAlerts() {
    final now = DateTime.now();
    return [
      AlertEvent(
        id: _uuid.v4(),
        timestamp: now.subtract(const Duration(minutes: 2)),
        type: AlertType.boot,
        severity: AlertSeverity.success,
        icon: '✅',
        title: 'System online',
        body: '✅ System is online and booted successfully!',
      ),
      AlertEvent(
        id: _uuid.v4(),
        timestamp: now.subtract(const Duration(hours: 3)),
        type: AlertType.tankEmpty,
        severity: AlertSeverity.success,
        icon: '🚰',
        title: 'Tank empty',
        body: '🚰 Tank is empty! Motor 2 started automatically. ✅',
      ),
      AlertEvent(
        id: _uuid.v4(),
        timestamp: now.subtract(const Duration(hours: 6)),
        type: AlertType.report,
        severity: AlertSeverity.info,
        icon: '📊',
        title: 'EcoSense report',
        body:
            '🔋 100%  ·  ⚡ 36.3V  ·  🌡️ 31.2°C  ·  💧 42%  ·  🌧️ None  ·  🚰 Empty 💧',
      ),
    ];
  }

  List<SensorReading> _seedHistory() {
    final now = DateTime.now();
    return [
      for (var i = 48; i >= 1; i--)
        SensorReading(
          timestamp: now.subtract(Duration(minutes: i * 30)),
          temperatureC: 28 + (i % 7) * 0.6,
          soilMoisture: 35 + (i % 11) * 2.0,
          batteryPercent: (100 - i * 0.3).clamp(40, 100),
          voltage: 35.8 + (i % 5) * 0.15,
        ),
    ];
  }

  String fullStatusReport() {
    final s = state.status;
    final r = s.reading;
    final forecast = s.rainLikely24h
        ? '⚠️ Rain likely in the next 24 hours.'
        : 'Clear skies expected in the next 24 hours.';
    return [
      'Shoaib: EcoSense Report',
      '🔋 Battery charge — 🔋 ${Formatters.percent(r.batteryPercent)}',
      '⚡ Voltage — ${Formatters.voltage(r.voltage)}',
      '🌡️ Temperature — ${r.temperatureFault ? 'Sensor Error' : '${r.temperatureC.toStringAsFixed(1)}°C'}',
      '💧 Soil moisture — ${Formatters.percent(r.soilMoisture)}',
      '🌧️ Current rain — ${Formatters.rain(s.raining)}',
      '🚰 Tank status — ${Formatters.tank(s.tankFull)}',
      '🔌 Tank motor — ${Formatters.motor(s.motor2On)}',
      '🔮 $forecast',
    ].join('\n');
  }

  @override
  void dispose() {
    _sub?.cancel();
    _netSub?.cancel();
    _repo?.dispose();
    super.dispose();
  }
}

final localStoreProvider = Provider<LocalStore>((ref) => LocalStore.instance);

final notificationServiceProvider =
    Provider<NotificationService>((ref) => NotificationService());

final deviceControllerProvider =
    StateNotifierProvider<DeviceController, DeviceViewState>((ref) {
  return DeviceController(
    settings: ref.watch(settingsControllerProvider.notifier),
    auth: ref.watch(authServiceProvider),
    store: ref.watch(localStoreProvider),
    notifications: ref.watch(notificationServiceProvider),
  );
});
