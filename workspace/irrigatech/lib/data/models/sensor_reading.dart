class SensorReading {
  const SensorReading({
    required this.timestamp,
    required this.temperatureC,
    required this.soilMoisture,
    required this.batteryPercent,
    required this.voltage,
  });

  final DateTime timestamp;
  final double temperatureC;
  final double soilMoisture;
  final double batteryPercent;
  final double voltage;

  bool get temperatureFault =>
      temperatureC == -127.0 || temperatureC < -50;

  Map<String, dynamic> toMap() => {
        'ts': timestamp.toIso8601String(),
        't': temperatureC,
        'm': soilMoisture,
        'b': batteryPercent,
        'v': voltage,
      };

  factory SensorReading.fromMap(Map<dynamic, dynamic> map) {
    return SensorReading(
      timestamp: DateTime.parse(map['ts'] as String),
      temperatureC: (map['t'] as num).toDouble(),
      soilMoisture: (map['m'] as num).toDouble(),
      batteryPercent: (map['b'] as num).toDouble(),
      voltage: (map['v'] as num).toDouble(),
    );
  }
}
