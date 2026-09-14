import '../models/device_status.dart';

/// Contract for Demo and Live (Blynk) data sources.
abstract class IrrigationRepository {
  Stream<DeviceStatus> watchStatus({required Duration pollInterval});

  Future<DeviceStatus> fetchOnce();

  /// Write motor pin. Returns the resulting status after the write.
  Future<DeviceStatus> setMotor({
    required int motorIndex,
    required bool on,
  });

  Future<bool> testConnection();

  void dispose();
}
