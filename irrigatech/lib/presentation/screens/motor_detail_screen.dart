import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/formatters.dart';
import '../../domain/device_controller.dart';
import '../widgets/glass_card.dart';

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
        ? 'Motor 1 is disabled while raining and auto-turns-off when rain starts. The hardware interlock cannot be overridden from the app.'
        : 'Motor 2 is disabled while the tank is full. It auto-turns-off when full and auto-turns-on when empty. A buzzer fires on the device when full.';

    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          GlassCard(
            child: Column(
              children: [
                Icon(
                  Icons.power_settings_new,
                  size: 72,
                  color: on ? AppColors.success : Theme.of(context).disabledColor,
                ),
                const SizedBox(height: 8),
                Text(
                  on ? 'ON 🟢' : 'OFF 🔴',
                  style: Theme.of(context)
                      .textTheme
                      .headlineSmall
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 16),
                Switch.adaptive(
                  value: on,
                  onChanged: locked
                      ? null
                      : (v) => ref
                          .read(deviceControllerProvider.notifier)
                          .toggleMotor(motorIndex, v),
                ),
                if (locked) ...[
                  const SizedBox(height: 8),
                  Icon(
                    isField ? Icons.umbrella : Icons.water,
                    color: AppColors.warning,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 12),
          GlassCard(
            tint: AppColors.warning.withOpacity(0.08),
            child: Text(reason),
          ),
          const SizedBox(height: 16),
          Text(
            'On / off history',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(fontWeight: FontWeight.w700),
          ),
          const SizedBox(height: 8),
          if (device.motorHistory.isEmpty)
            const GlassCard(child: Text('No motor events recorded yet.')),
          ...device.motorHistory.reversed.take(30).map((m) {
            final value = isField ? m.motor1On : m.motor2On;
            return ListTile(
              leading: Icon(
                value ? Icons.play_arrow : Icons.stop,
                color: value ? AppColors.success : AppColors.danger,
              ),
              title: Text(value ? 'Turned ON' : 'Turned OFF'),
              subtitle: Text(Formatters.dateTime.format(m.changedAt)),
            );
          }),
        ],
      ),
    );
  }
}
