enum AlertSeverity { info, success, warning, critical, security }

enum AlertType {
  report,
  rain,
  tankEmpty,
  tankFull,
  motorAuto,
  boot,
  tamper,
  lowBattery,
  sensorError,
  connection,
}

class AlertEvent {
  const AlertEvent({
    required this.id,
    required this.timestamp,
    required this.type,
    required this.severity,
    required this.title,
    required this.body,
    this.icon = '📊',
  });

  final String id;
  final DateTime timestamp;
  final AlertType type;
  final AlertSeverity severity;
  final String title;
  final String body;
  final String icon;

  bool get isSecurity => type == AlertType.tamper;

  Map<String, dynamic> toMap() => {
        'id': id,
        'ts': timestamp.toIso8601String(),
        'type': type.name,
        'sev': severity.name,
        'title': title,
        'body': body,
        'icon': icon,
      };

  factory AlertEvent.fromMap(Map<dynamic, dynamic> map) {
    return AlertEvent(
      id: map['id'] as String,
      timestamp: DateTime.parse(map['ts'] as String),
      type: AlertType.values.firstWhere(
        (e) => e.name == map['type'],
        orElse: () => AlertType.report,
      ),
      severity: AlertSeverity.values.firstWhere(
        (e) => e.name == map['sev'],
        orElse: () => AlertSeverity.info,
      ),
      title: map['title'] as String,
      body: map['body'] as String,
      icon: (map['icon'] as String?) ?? '📊',
    );
  }
}
