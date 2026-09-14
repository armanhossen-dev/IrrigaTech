import 'dart:async';
import 'dart:math';

import '../../core/constants/app_constants.dart';
import '../models/device_status.dart';
import '../models/sensor_reading.dart';
import 'irrigation_repository.dart';

/// Fake but realistic streaming values so the app is fully demoable without hardware.
class DemoDataRepository implements IrrigationRepository {
  DemoDataRepository({Random? random}) : _rng = random ?? Random();

  final Random _rng;
  DeviceStatus _state = DeviceStatus.demoSeed();
  StreamController<DeviceStatus>? _controller;
  Timer? _timer;
  int _tick = 0;

  @override
  Stream<DeviceStatus> watchStatus({required Duration pollInterval}) {
    _controller?.close();
    _controller = StreamController<DeviceStatus>.broadcast();
    _emit();
    _timer?.cancel();
    _timer = Timer.periodic(pollInterval, (_) {
      _advance();
      _emit();
    });
    return _controller!.stream;
  }

  @override
  Future<DeviceStatus> fetchOnce() async {
    return _state;
  }

  @override
  Future<DeviceStatus> setMotor({
    required int motorIndex,
    required bool on,
  }) async {
    if (motorIndex == 1) {
      if (_state.raining && on) {
        return _state;
      }
      _state = _state.copyWith(motor1On: on, lastUpdated: DateTime.now());
    } else {
      if (_state.tankFull && on) {
        return _state;
      }
      _state = _state.copyWith(motor2On: on, lastUpdated: DateTime.now());
    }
    _emit();
    return _state;
  }

  @override
  Future<bool> testConnection() async {
    await Future<void>.delayed(const Duration(milliseconds: 400));
    return true;
  }

  @override
  void dispose() {
    _timer?.cancel();
    _controller?.close();
  }

  void _emit() {
    _controller?.add(_state);
  }

  void _advance() {
    _tick++;
    final now = DateTime.now();
    final r = _state.reading;

    final temp = (r.temperatureC + _noise(0.35)).clamp(18.0, 42.0);
    final moisture = (r.soilMoisture + _noise(1.8)).clamp(0.0, 100.0);
    final battery = (r.batteryPercent - 0.01).clamp(12.0, 100.0);
    final voltage = (r.voltage + _noise(0.08)).clamp(32.0, 38.5);

    var raining = _state.raining;
    var tankFull = _state.tankFull;
    var motor1 = _state.motor1On;
    var motor2 = _state.motor2On;
    var wrong = _state.wrongKeypadAttempts;
    var rainLikely = _state.rainLikely24h;

    // Cycle rain every ~40 ticks (~2 min at 3s).
    if (_tick % 40 == 12) {
      raining = true;
      motor1 = false;
    }
    if (_tick % 40 == 22) {
      raining = false;
    }

    // Tank fills while motor 2 is on, empties slowly otherwise.
    if (motor2 && !tankFull && _tick % 18 == 0) {
      tankFull = true;
      motor2 = false;
    } else if (tankFull && _tick % 25 == 5) {
      tankFull = false;
      motor2 = true;
    }

    if (_tick % 90 == 55) {
      wrong = 3;
    } else if (_tick % 90 == 70) {
      wrong = 0;
    }

    rainLikely = raining || _tick % 50 > 35;

    // Occasional temperature sentinel so UI shows Sensor Error.
    final useSentinel = _tick % 80 == 33;

    _state = DeviceStatus(
      reading: SensorReading(
        timestamp: now,
        temperatureC: useSentinel
            ? AppConstants.temperatureErrorSentinel
            : double.parse(temp.toStringAsFixed(1)),
        soilMoisture: double.parse(moisture.toStringAsFixed(0)),
        batteryPercent: double.parse(battery.toStringAsFixed(0)),
        voltage: double.parse(voltage.toStringAsFixed(1)),
      ),
      motor1On: motor1,
      motor2On: motor2,
      raining: raining,
      tankFull: tankFull,
      connected: true,
      rainLikely24h: rainLikely,
      wrongKeypadAttempts: wrong,
      lastUpdated: now,
    );
  }

  double _noise(double amp) => (_rng.nextDouble() * 2 - 1) * amp;
}
