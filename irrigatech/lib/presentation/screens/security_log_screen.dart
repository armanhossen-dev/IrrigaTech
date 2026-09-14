import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/formatters.dart';
import '../../data/models/alert_event.dart';
import '../../domain/device_controller.dart';
import '../widgets/glass_card.dart';

class SecurityLogScreen extends ConsumerWidget {
  const SecurityLogScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final device = ref.watch(deviceControllerProvider);
    final tampers =
        device.alerts.where((a) => a.type == AlertType.tamper).toList();
    final attempts = device.status.wrongKeypadAttempts;

    return Scaffold(
      appBar: AppBar(title: const Text('Security log')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          GlassCard(
            tint: attempts >= 3 ? AppColors.danger.withValues(alpha: 0.12) : null,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Keypad access summary',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
                ),
                const SizedBox(height: 12),
                Text('Wrong-attempt counter: $attempts / 3'),
                const SizedBox(height: 4),
                Text(
                  attempts >= 3
                      ? '🚨 Tamper threshold reached. Physical keypad locked out.'
                      : 'Keypad is within the allowed attempt window.',
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'Tamper timestamps',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 8),
          if (tampers.isEmpty)
            const GlassCard(child: Text('No tamper events recorded.')),
          ...tampers.map(
            (a) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: GlassCard(
                tint: AppColors.danger.withValues(alpha: 0.12),
                child: ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Text('🚨', style: TextStyle(fontSize: 24)),
                  title: Text(a.title),
                  subtitle: Text(
                    '${a.body}\n${Formatters.dateTime.format(a.timestamp)}',
                  ),
                  isThreeLine: true,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
