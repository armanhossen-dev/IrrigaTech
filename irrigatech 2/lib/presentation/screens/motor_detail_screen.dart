import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../core/theme/status_visuals.dart';
import '../../core/utils/formatters.dart';
import '../../domain/device_controller.dart';
import '../widgets/glass_card.dart';
import '../widgets/status_icon.dart';

class MotorDetailScreen extends ConsumerWidget {
  const MotorDetailScreen({super.key, required this.motorIndex});

  final int motorIndex;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final device = ref.watch(deviceControllerProvider);
    final s = device.status;
    final isField = motorIndex == 1;
    final on = isField ? s.motor1On : s.motor2On;
    final locked = isField ? s.motor1Locked : s.motor2Locked;
    final title = isField ? 'Motor 1 · Field pump' : 'Motor 2 · Tank pump';
    final reason = isField
        ? 'Motor 1 is disabled while raining and turns off automatically when rain starts. The hardware interlock cannot be overridden from the app.'
        : 'Motor 2 is disabled while the tank is full. It turns off automatically when full and on when empty. A buzzer fires on the device when full.';
    final theme = Theme.of(context);
    final statusColor = on ? AppColors.success : theme.colorScheme.outline;

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView(
        padding: const EdgeInsets.all(AppSpace.lg),
        children: [
          GlassCard(
            child: Column(
              children: [
                StatusIcon(
                  icon: Icons.power_settings_new,
                  color: statusColor,
                ),
                const SizedBox(height: AppSpace.md),
                Text(
                  on ? 'On' : 'Off',
                  style: theme.textTheme.titleMedium?.copyWith(color: statusColor),
                ),
                const SizedBox(height: AppSpace.lg),
                Switch.adaptive(
                  value: on,
                  onChanged: locked
                      ? null
                      : (v) => ref
                          .read(deviceControllerProvider.notifier)
                          .toggleMotor(motorIndex, v),
                ),
                if (locked) ...[
                  const SizedBox(height: AppSpace.sm),
                  Icon(
                    isField ? StatusVisuals.rain : StatusVisuals.tank,
                    color: AppColors.warning,
                    size: AppSpace.iconLg,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    isField ? 'Locked: rain detected' : 'Locked: tank full',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: AppColors.warning,
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: AppSpace.md),
          GlassCard(
            tint: AppColors.warning.withOpacity(0.06),
            child: Text(reason, style: theme.textTheme.bodyMedium),
          ),
          const SizedBox(height: AppSpace.lg),
          Text('On / off history', style: theme.textTheme.titleMedium),
          const SizedBox(height: AppSpace.sm),
          if (device.motorHistory.isEmpty)
            const GlassCard(child: Text('No motor events recorded yet.')),
          ...device.motorHistory.reversed.take(30).map((m) {
            final value = isField ? m.motor1On : m.motor2On;
            return Padding(
              padding: const EdgeInsets.only(bottom: AppSpace.sm),
              child: GlassCard(
                padding: const EdgeInsets.symmetric(
                  horizontal: AppSpace.md,
                  vertical: 10,
                ),
                child: Row(
                  children: [
                    Icon(
                      value ? Icons.play_arrow : Icons.stop,
                      size: AppSpace.icon,
                      color: value ? AppColors.success : AppColors.danger,
                    ),
                    const SizedBox(width: AppSpace.md),
                    Expanded(
                      child: Text(
                        value ? 'Turned on' : 'Turned off',
                        style: theme.textTheme.titleSmall,
                      ),
                    ),
                    Text(
                      Formatters.dateTime.format(m.changedAt),
                      style: theme.textTheme.bodySmall,
                    ),
                  ],
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}
