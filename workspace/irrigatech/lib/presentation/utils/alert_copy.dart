import '../../data/models/alert_event.dart';

/// Presentation-only copy. Domain strings may still contain emoji glyphs;
/// this layer strips them and applies neutral operational phrasing.
class AlertCopy {
  static String title(AlertEvent event) {
    return switch (event.type) {
      AlertType.boot => 'System online',
      AlertType.rain => 'Rain detected',
      AlertType.tankFull => 'Tank full',
      AlertType.tankEmpty => 'Tank empty',
      AlertType.tamper => 'Security alert',
      AlertType.lowBattery => 'Low battery',
      AlertType.sensorError => 'Sensor error',
      AlertType.connection => 'Connection lost',
      AlertType.motorAuto => 'Motor auto action',
      AlertType.report => 'Status report',
    };
  }

  static String body(AlertEvent event) {
    return switch (event.type) {
      AlertType.boot =>
        'Controller booted and is reporting telemetry.',
      AlertType.rain =>
        'Field pump locked. Motor 1 turned off automatically.',
      AlertType.tankFull =>
        'Tank pump stopped automatically. Device buzzer triggered.',
      AlertType.tankEmpty =>
        'Tank empty. Motor 2 started automatically.',
      AlertType.tamper =>
        'Three or more incorrect keypad codes. Access attempt blocked.',
      AlertType.lowBattery => _strip(event.body),
      AlertType.sensorError =>
        'Temperature probe returned the firmware sentinel (-127.0). Check wiring.',
      AlertType.connection =>
        'Blynk API is unreachable. Showing last known values.',
      AlertType.motorAuto => _strip(event.body),
      AlertType.report => _reportBody(event.body),
    };
  }

  static String _reportBody(String raw) {
    final stripped = _strip(raw);
    if (stripped.contains('Battery') || stripped.contains('Voltage')) {
      return stripped;
    }
    return 'Latest telemetry snapshot. Open system status for the full field list.';
  }

  static String _strip(String raw) {
    const glyphs = [
      '🔋',
      '⚡',
      '🌡️',
      '💧',
      '🌧️',
      '🚰',
      '🔌',
      '🔮',
      '🚨',
      '✅',
      '🟢',
      '🔴',
      '🛑',
      '📊',
      '⚠️',
    ];
    var out = raw;
    for (final glyph in glyphs) {
      out = out.replaceAll(glyph, '');
    }
    return out.replaceAll(RegExp(r'\s+'), ' ').trim();
  }
}
