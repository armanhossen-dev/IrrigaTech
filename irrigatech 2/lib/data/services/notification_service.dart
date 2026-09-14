import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// Local notifications plus FCM. Firebase Messaging needs a real
/// google-services.json; init() still succeeds without it.
class NotificationService {
  NotificationService() : _plugin = FlutterLocalNotificationsPlugin();

  final FlutterLocalNotificationsPlugin _plugin;
  bool _ready = false;

  Future<void> init() async {
    const android = AndroidInitializationSettings('@mipmap/ic_launcher');
    const ios = DarwinInitializationSettings();
    await _plugin.initialize(
      const InitializationSettings(android: android, iOS: ios),
    );
    _ready = true;
    try {
      // FCM: request permission and keep the token; AuthService upserts it.
      await FirebaseMessaging.instance.requestPermission();
      FirebaseMessaging.onMessage.listen((message) {
        final n = message.notification;
        if (n != null) {
          show(id: n.hashCode, title: n.title ?? 'IrrigaTech', body: n.body ?? '');
        }
      });
    } catch (_) {}
  }

  Future<void> show({
    required int id,
    required String title,
    required String body,
  }) async {
    if (!_ready) return;
    const details = NotificationDetails(
      android: AndroidNotificationDetails(
        'irrigatech_alerts',
        'IrrigaTech Alerts',
        channelDescription: 'Tank, rain, tamper and battery alerts',
        importance: Importance.high,
        priority: Priority.high,
      ),
      iOS: DarwinNotificationDetails(),
    );
    await _plugin.show(id, title, body, details);
  }
}
