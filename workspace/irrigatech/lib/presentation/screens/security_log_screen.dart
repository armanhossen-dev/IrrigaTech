import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../core/theme/status_visuals.dart';
import '../../core/utils/formatters.dart';
import '../../data/models/alert_event.dart';
import '../../domain/device_controller.dart';
import '../utils/alert_copy.dart';
import '../widgets/glass_card.dart';
import '../widgets/status_icon.dart';

class SecurityLogScreen extends ConsumerWidget {
  const SecurityLogScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final device = ref.watch(deviceControllerProvider);
    final tampers =
        device.alerts.where((a) => a.type == AlertType.tamper).toList();
    final attempts = device.status.wrongKeypadAttempts;
    final locked = attempts >= 3;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('Security log')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpace.lg),
        children: [
          GlassCard(
            tint: locked ? AppColors.danger.withOpacity(0.08) : null,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    StatusIcon(
                      icon: locked
                          ? StatusVisuals.security
                          : Icons.verified_user_outlined,
                      color: locked ? AppColors.danger : AppColors.success,
                    ),
                    const SizedBox(width: AppSpace.md),
                    Expanded(
                      child: Text(
                        'Keypad access summary',
                        style: theme.textTheme.titleSmall,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: AppSpace.md),
                Text(
                  'Wrong-attempt counter: $attempts / 3',
                  style: theme.textTheme.bodyMedium,
                ),
                const SizedBox(height: 4),
                Text(
                  locked
                      ? 'Tamper threshold reached. Physical keypad locked out.'
                      : 'Keypad is within the allowed attempt window.',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: locked ? AppColors.danger : null,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          Text('Tamper timestamps', style: theme.textTheme.titleMedium),
          const SizedBox(height: AppSpace.sm),
          if (tampers.isEmpty)
            const GlassCard(child: Text('No tamper events recorded.')),
          ...tampers.map(
            (a) => Padding(
              padding: const EdgeInsets.only(bottom: AppSpace.sm),
              child: GlassCard(
                tint: AppColors.danger.withOpacity(0.08),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const StatusIcon(
                      icon: StatusVisuals.security,
                      color: AppColors.danger,
                    ),
                    const SizedBox(width: AppSpace.md),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            AlertCopy.title(a),
                            style: theme.textTheme.titleSmall,
                          ),
                          const SizedBox(height: 4),
                          Text(
                            AlertCopy.body(a),
                            style: theme.textTheme.bodySmall,
                          ),
                          const SizedBox(height: 6),
                          Text(
                            Formatters.dateTime.format(a.timestamp),
                            style: theme.textTheme.labelSmall,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
