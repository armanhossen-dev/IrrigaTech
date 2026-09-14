import 'package:flutter_test/flutter_test.dart';

import 'package:irrigatech/core/constants/app_constants.dart';
import 'package:irrigatech/core/utils/formatters.dart';
import 'package:irrigatech/data/models/device_status.dart';
import 'package:irrigatech/data/repositories/demo_data_repository.dart';

void main() {
  test('temperature sentinel is reported as Sensor Error', () {
    expect(
      Formatters.temperature(
        AppConstants.temperatureErrorSentinel,
        useCelsius: true,
      ),
      'Sensor Error',
    );
  });

  test('motor interlocks lock the correct pumps', () {
    final raining = DeviceStatus.demoSeed().copyWith(raining: true, motor1On: false);
    expect(raining.motor1Locked, isTrue);
    expect(raining.motor2Locked, isFalse);

    final full = DeviceStatus.demoSeed().copyWith(tankFull: true, motor2On: false);
    expect(full.motor2Locked, isTrue);
    expect(full.motor1Locked, isFalse);
  });

  test('demo repository streams realistic values', () async {
    final repo = DemoDataRepository();
    final first = await repo.fetchOnce();
    expect(first.connected, isTrue);
    expect(first.reading.batteryPercent, greaterThan(0));
    repo.dispose();
  });

  test('EcoSense report field order helpers', () {
    expect(Formatters.rain(false), 'None');
    expect(Formatters.tank(true), 'Full 🛑');
    expect(Formatters.motor(true), 'ON 🟢');
  });
}
