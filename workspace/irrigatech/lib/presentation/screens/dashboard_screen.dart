import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';
import '../../core/theme/status_visuals.dart';
import '../../core/utils/formatters.dart';
import '../../domain/device_controller.dart';
import '../../domain/settings_controller.dart';
import '../widgets/alert_tile.dart';
import '../widgets/glass_card.dart';
import '../widgets/motor_card.dart';
import '../widgets/sensor_tile.dart';
import '../widgets/status_icon.dart';
import '../widgets/status_report_sheet.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final device = ref.watch(deviceControllerProvider);
    final settings = ref.watch(settingsControllerProvider);
    final s = device.status;
    final r = s.reading;
    final hist = device.history;
    final last = hist.length > 18 ? hist.sublist(hist.length - 18) : hist;
    final theme = Theme.of(context);

    List<double> series(double Function(dynamic e) pick) =>
        last.map((e) => pick(e)).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('IrrigaTech'),
        actions: [
          IconButton(
            tooltip: 'System status',
            onPressed: () {
              showModalBottomSheet<void>(
                context: context,
                isScrollControlled: true,
                builder: (_) => StatusReportSheet(status: s),
              );
            },
            icon: const Icon(Icons.article_outlined, size: AppSpace.iconLg),
          ),
          IconButton(
            tooltip: 'Security log',
            onPressed: () => context.push('/security'),
            icon: const Icon(Icons.security_outlined, size: AppSpace.iconLg),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(deviceControllerProvider.notifier).refresh(),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(AppSpace.lg, AppSpace.sm, AppSpace.lg, AppSpace.xl),
          children: [
            _ConnectionPill(
              connected: s.connected,
              demo: device.usingDemo,
              updated: s.lastUpdated,
            ),
            const SizedBox(height: AppSpace.md),
            GlassCard(
              child: Row(
                children: [
                  StatusIcon(
                    icon: s.raining
                        ? StatusVisuals.rain
                        : Icons.wb_sunny_outlined,
                    color: s.raining ? AppColors.rain : AppColors.warning,
                  ),
                  const SizedBox(width: AppSpace.md),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          s.raining ? 'Rain on site' : 'No rain on site',
                          style: theme.textTheme.titleSmall,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          s.rainLikely24h
                              ? 'Rain likely in the next 24 hours.'
                              : 'Clear skies expected in the next 24 hours.',
                          style: theme.textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: AppSpace.md),
            Row(
              children: [
                Expanded(
                  child: GlassCard(
                    tint: s.raining ? AppColors.rain.withOpacity(0.08) : null,
                    padding: const EdgeInsets.all(AppSpace.md),
                    child: Row(
                      children: [
                        Icon(
                          StatusVisuals.rain,
                          size: AppSpace.icon,
                          color: s.raining ? AppColors.rain : AppColors.teal,
                        ),
                        const SizedBox(width: AppSpace.sm),
                        Expanded(
                          child: Text(
                            'Rain: ${Formatters.rain(s.raining)}',
                            style: theme.textTheme.titleSmall,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: AppSpace.md),
                Expanded(
                  child: GlassCard(
                    tint: s.tankFull ? AppColors.danger.withOpacity(0.08) : null,
                    padding: const EdgeInsets.all(AppSpace.md),
                    child: Row(
                      children: [
                        Icon(
                          s.tankFull
                              ? StatusVisuals.tankFull
                              : StatusVisuals.tank,
                          size: AppSpace.icon,
                          color: s.tankFull ? AppColors.danger : AppColors.teal,
                        ),
                        const SizedBox(width: AppSpace.sm),
                        Expanded(
                          child: Text(
                            'Tank: ${s.tankFull ? 'Full' : 'Empty'}',
                            style: theme.textTheme.titleSmall,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: AppSpace.lg),
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: AppSpace.md,
              crossAxisSpacing: AppSpace.md,
              childAspectRatio: 1.18,
              children: [
                SensorTile(
                  label: 'Temperature',
                  value: Formatters.temperature(
                    r.temperatureC,
                    useCelsius: settings.useCelsius,
                  ),
                  icon: StatusVisuals.temperature,
                  color: AppColors.warning,
                  spark: series((e) => e.temperatureC),
                  error: r.temperatureFault,
                ),
                SensorTile(
                  label: 'Soil moisture',
                  value: Formatters.percent(r.soilMoisture),
                  icon: StatusVisuals.moisture,
                  color: AppColors.teal,
                  spark: series((e) => e.soilMoisture),
                ),
                SensorTile(
                  label: 'Battery',
                  value: Formatters.percent(r.batteryPercent),
                  icon: StatusVisuals.battery,
                  color: AppColors.success,
                  spark: series((e) => e.batteryPercent),
                ),
                SensorTile(
                  label: 'Voltage',
                  value: Formatters.voltage(r.voltage),
                  icon: StatusVisuals.voltage,
                  color: AppColors.info,
                  spark: series((e) => e.voltage),
                ),
              ],
            ),
            const SizedBox(height: AppSpace.lg),
            MotorCard(
              title: 'Motor 1 · Field pump',
              subtitle: s.motor1On ? 'On' : 'Off',
              isOn: s.motor1On,
              locked: s.motor1Locked,
              lockIcon: StatusVisuals.rain,
              lockReason:
                  'Locked while raining. Field pump turns off automatically when rain starts.',
              onToggle: (v) =>
                  ref.read(deviceControllerProvider.notifier).toggleMotor(1, v),
              onOpen: () => context.push('/motor/1'),
            ),
            const SizedBox(height: AppSpace.md),
            MotorCard(
              title: 'Motor 2 · Tank pump',
              subtitle: s.motor2On ? 'On' : 'Off',
              isOn: s.motor2On,
              locked: s.motor2Locked,
              lockIcon: StatusVisuals.tank,
              lockReason:
                  'Locked while tank is full. Auto-off when full, auto-on when empty.',
              onToggle: (v) =>
                  ref.read(deviceControllerProvider.notifier).toggleMotor(2, v),
              onOpen: () => context.push('/motor/2'),
            ),
            const SizedBox(height: AppSpace.lg),
            Row(
              children: [
                Text('Recent alerts', style: theme.textTheme.titleMedium),
                const Spacer(),
                TextButton(
                  onPressed: () => context.go('/alerts'),
                  child: const Text('See all'),
                ),
              ],
            ),
            if (device.alerts.isEmpty)
              const GlassCard(child: Text('No alerts recorded.')),
            ...device.alerts.take(4).map(
                  (a) => Padding(
                    padding: const EdgeInsets.only(bottom: AppSpace.sm),
                    child: AlertTile(
                      event: a,
                      compact: true,
                      onTap: () => context.go('/alerts'),
                    ),
                  ),
                ),
          ],
        ),
      ),
    );
  }
}

class _ConnectionPill extends StatelessWidget {
  const _ConnectionPill({
    required this.connected,
    required this.demo,
    required this.updated,
  });

  final bool connected;
  final bool demo;
  final DateTime updated;

  @override
  Widget build(BuildContext context) {
    final label = demo
        ? 'Demo mode'
        : connected
            ? 'Live · Blynk'
            : 'Offline';
    final color = demo
        ? AppColors.teal
        : connected
            ? AppColors.success
            : AppColors.danger;
    return Align(
      alignment: Alignment.centerLeft,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
        decoration: BoxDecoration(
          color: color.withOpacity(0.10),
          borderRadius: BorderRadius.circular(99),
          border: Border.all(color: color.withOpacity(0.22)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 7,
              height: 7,
              decoration: BoxDecoration(color: color, shape: BoxShape.circle),
            ),
            const SizedBox(width: 8),
            Text(
              '$label · ${Formatters.time.format(updated)}',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                    color: color,
                    fontWeight: FontWeight.w600,
                  ),
            ),
          ],
        ),
      ),
    );
  }
}
