import 'package:hive_flutter/hive_flutter.dart';

import '../../core/constants/app_constants.dart';
import '../models/alert_event.dart';
import '../models/device_status.dart';
import '../models/sensor_reading.dart';

class LocalStore {
  LocalStore._();

  static LocalStore? _instance;
  static LocalStore get instance => _instance ??= LocalStore._();

  late Box<Map> _history;
  late Box<Map> _alerts;
  late Box _settings;

  Future<void> init() async {
    await Hive.initFlutter();
    _history = await Hive.openBox<Map>(AppConstants.hiveBoxHistory);
    _alerts = await Hive.openBox<Map>(AppConstants.hiveBoxAlerts);
    _settings = await Hive.openBox(AppConstants.hiveBoxSettings);
  }

  Future<void> addReading(SensorReading reading) async {
    await _history.add(reading.toMap());
    if (_history.length > 4000) {
      await _history.deleteAt(0);
    }
  }

  List<SensorReading> readingsSince(DateTime from) {
    return _history.values
        .map(SensorReading.fromMap)
        .where((r) => r.timestamp.isAfter(from))
        .toList()
      ..sort((a, b) => a.timestamp.compareTo(b.timestamp));
  }

  Future<void> addMotorSnapshot(MotorSnapshot snap) async {
    final box = await Hive.openBox<Map>('motor_history');
    await box.add(snap.toMap());
    if (box.length > 2000) {
      await box.deleteAt(0);
    }
  }

  Future<List<MotorSnapshot>> motorHistory() async {
    final box = await Hive.openBox<Map>('motor_history');
    return box.values.map(MotorSnapshot.fromMap).toList()
      ..sort((a, b) => a.changedAt.compareTo(b.changedAt));
  }

  Future<void> addAlert(AlertEvent event) async {
    await _alerts.put(event.id, event.toMap());
  }

  List<AlertEvent> alerts() {
    return _alerts.values.map(AlertEvent.fromMap).toList()
      ..sort((a, b) => b.timestamp.compareTo(a.timestamp));
  }

  T? getSetting<T>(String key) => _settings.get(key) as T?;

  Future<void> setSetting(String key, dynamic value) =>
      _settings.put(key, value);
}
