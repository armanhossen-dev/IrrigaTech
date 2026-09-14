import 'dart:async';

import 'package:dio/dio.dart';

import '../../core/constants/app_constants.dart';
import '../models/device_status.dart';
import '../models/sensor_reading.dart';
import 'irrigation_repository.dart';

/// Live Blynk REST client.
///
/// Read:  GET https://blynk.cloud/external/api/get?token={TOKEN}&V0
/// Write: GET https://blynk.cloud/external/api/update?token={TOKEN}&V2=1
class BlynkRepository implements IrrigationRepository {
  BlynkRepository({
    required this.token,
    Dio? dio,
  }) : _dio = dio ??
            Dio(
              BaseOptions(
                baseUrl: AppConstants.blynkBaseUrl,
                connectTimeout: const Duration(seconds: 8),
                receiveTimeout: const Duration(seconds: 8),
              ),
            );

  final String token;
  final Dio _dio;
  StreamController<DeviceStatus>? _controller;
  Timer? _timer;
  DeviceStatus _last = DeviceStatus.demoSeed().copyWith(connected: false);

  @override
  Stream<DeviceStatus> watchStatus({required Duration pollInterval}) {
    _controller?.close();
    _controller = StreamController<DeviceStatus>.broadcast();
    _poll();
    _timer?.cancel();
    _timer = Timer.periodic(pollInterval, (_) => _poll());
    return _controller!.stream;
  }

  @override
  Future<DeviceStatus> fetchOnce() => _fetch();

  @override
  Future<DeviceStatus> setMotor({
    required int motorIndex,
    required bool on,
  }) async {
    final pin = motorIndex == 1
        ? AppConstants.pinMotor1
        : AppConstants.pinMotor2;
    // Blynk write: /update?token=...&V2=1
    await _dio.get<dynamic>(
      '/update',
      queryParameters: {
        'token': token,
        pin: on ? 1 : 0,
      },
    );
    return _fetch();
  }

  @override
  Future<bool> testConnection() async {
    try {
      await _dio.get<dynamic>(
        '/isHardwareConnected',
        queryParameters: {'token': token},
      );
      await _dio.get<dynamic>(
        '/get',
        queryParameters: {
          'token': token,
          AppConstants.pinTemperature: '',
        },
      );
      return true;
    } catch (_) {
      return false;
    }
  }

  @override
  void dispose() {
    _timer?.cancel();
    _controller?.close();
  }

  Future<void> _poll() async {
    try {
      final status = await _fetch();
      _last = status;
      _controller?.add(status);
    } catch (e) {
      _last = _last.copyWith(
        connected: false,
        lastError: 'Blynk API unreachable',
        lastUpdated: DateTime.now(),
      );
      _controller?.add(_last);
    }
  }

  Future<DeviceStatus> _fetch() async {
    // Batch-read all virtual pins used by EcoSense/IrrigaTech.
    // Blynk batch read: /get?token=...&V0&V1&V2&V3&V4&V5
    final response = await _dio.get<dynamic>(
      '/get?token=$token'
      '&${AppConstants.pinTemperature}'
      '&${AppConstants.pinMoisture}'
      '&${AppConstants.pinMotor1}'
      '&${AppConstants.pinMotor2}'
      '&${AppConstants.pinBattery}'
      '&${AppConstants.pinVoltage}',
    );

    final data = _normalize(response.data);
    final now = DateTime.now();
    final temp = _asDouble(data[AppConstants.pinTemperature]);
    final moisture = _asDouble(data[AppConstants.pinMoisture]);
    final motor1 = _asBool(data[AppConstants.pinMotor1]);
    final motor2 = _asBool(data[AppConstants.pinMotor2]);
    final battery = _asDouble(data[AppConstants.pinBattery]);
    final voltage = _asDouble(data[AppConstants.pinVoltage]);

    return DeviceStatus(
      reading: SensorReading(
        timestamp: now,
        temperatureC: temp,
        soilMoisture: moisture,
        batteryPercent: battery,
        voltage: voltage,
      ),
      motor1On: motor1,
      motor2On: motor2,
      raining: _last.raining,
      tankFull: _last.tankFull,
      connected: true,
      rainLikely24h: _last.rainLikely24h,
      wrongKeypadAttempts: _last.wrongKeypadAttempts,
      lastUpdated: now,
    );
  }

  Map<String, dynamic> _normalize(dynamic raw) {
    if (raw is Map) {
      return raw.map((k, v) => MapEntry(k.toString(), v));
    }
    if (raw is List) {
      // Some Blynk responses return a positional list matching requested pins.
      const pins = [
        AppConstants.pinTemperature,
        AppConstants.pinMoisture,
        AppConstants.pinMotor1,
        AppConstants.pinMotor2,
        AppConstants.pinBattery,
        AppConstants.pinVoltage,
      ];
      final map = <String, dynamic>{};
      for (var i = 0; i < pins.length && i < raw.length; i++) {
        map[pins[i]] = raw[i];
      }
      return map;
    }
    return {};
  }

  double _asDouble(dynamic v) {
    if (v == null) return 0;
    if (v is num) return v.toDouble();
    if (v is List && v.isNotEmpty) return _asDouble(v.first);
    return double.tryParse(v.toString()) ?? 0;
  }

  bool _asBool(dynamic v) {
    if (v is bool) return v;
    if (v is num) return v != 0;
    if (v is List && v.isNotEmpty) return _asBool(v.first);
    final s = v?.toString().toLowerCase();
    return s == '1' || s == 'true' || s == 'on';
  }
}
