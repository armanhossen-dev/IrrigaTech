import 'sensor_reading.dart';

class MotorSnapshot {
  const MotorSnapshot({
    required this.motor1On,
    required this.motor2On,
    required this.changedAt,
  });

  final bool motor1On;
  final bool motor2On;
  final DateTime changedAt;

  Map<String, dynamic> toMap() => {
        'm1': motor1On,
        'm2': motor2On,
        'ts': changedAt.toIso8601String(),
      };

  factory MotorSnapshot.fromMap(Map<dynamic, dynamic> map) {
    return MotorSnapshot(
      motor1On: map['m1'] as bool,
      motor2On: map['m2'] as bool,
      changedAt: DateTime.parse(map['ts'] as String),
    );
  }
}

class DeviceStatus {
  const DeviceStatus({
    required this.reading,
    required this.motor1On,
    required this.motor2On,
    required this.raining,
    required this.tankFull,
    required this.connected,
    required this.rainLikely24h,
    required this.wrongKeypadAttempts,
    required this.lastUpdated,
    this.lastError,
  });

  final SensorReading reading;
  final bool motor1On;
  final bool motor2On;
  final bool raining;
  final bool tankFull;
  final bool connected;
  final bool rainLikely24h;
  final int wrongKeypadAttempts;
  final DateTime lastUpdated;
  final String? lastError;

  bool get motor1Locked => raining;
  bool get motor2Locked => tankFull;
  bool get tamperAlert => wrongKeypadAttempts >= 3;

  DeviceStatus copyWith({
    SensorReading? reading,
    bool? motor1On,
    bool? motor2On,
    bool? raining,
    bool? tankFull,
    bool? connected,
    bool? rainLikely24h,
    int? wrongKeypadAttempts,
    DateTime? lastUpdated,
    String? lastError,
    bool clearError = false,
  }) {
    return DeviceStatus(
      reading: reading ?? this.reading,
      motor1On: motor1On ?? this.motor1On,
      motor2On: motor2On ?? this.motor2On,
      raining: raining ?? this.raining,
      tankFull: tankFull ?? this.tankFull,
      connected: connected ?? this.connected,
      rainLikely24h: rainLikely24h ?? this.rainLikely24h,
      wrongKeypadAttempts: wrongKeypadAttempts ?? this.wrongKeypadAttempts,
      lastUpdated: lastUpdated ?? this.lastUpdated,
      lastError: clearError ? null : (lastError ?? this.lastError),
    );
  }

  static DeviceStatus demoSeed() {
    final now = DateTime.now();
    return DeviceStatus(
      reading: SensorReading(
        timestamp: now,
        temperatureC: 31.2,
        soilMoisture: 42,
        batteryPercent: 100,
        voltage: 36.3,
      ),
      motor1On: false,
      motor2On: true,
      raining: false,
      tankFull: false,
      connected: true,
      rainLikely24h: false,
      wrongKeypadAttempts: 0,
      lastUpdated: now,
    );
  }
}
