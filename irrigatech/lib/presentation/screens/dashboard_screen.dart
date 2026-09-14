import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/formatters.dart';
import '../../domain/device_controller.dart';
import '../../domain/settings_controller.dart';
import '../widgets/glass_card.dart';
import '../widgets/motor_card.dart';
import '../widgets/sensor_tile.dart';
import '../widgets/status_report_sheet.dart';
import '../widgets/weather_lottie.dart';

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

    List<double> series(double Function(dynamic e) pick) =>
        last.map((e) => pick(e)).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('IrrigaTech'),
        actions: [
          IconButton(
            tooltip: 'Full status report',
            onPressed: () {
              final report =
                  ref.read(deviceControllerProvider.notifier).fullStatusReport();
              showModalBottomSheet<void>(
                context: context,
                isScrollControlled: true,
                builder: (_) => StatusReportSheet(report: report),
              );
            },
            icon: const Icon(Icons.article_outlined),
          ),
          IconButton(
            tooltip: 'Security log',
            onPressed: () => context.push('/security'),
            icon: const Icon(Icons.security),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(deviceControllerProvider.notifier).refresh(),
        child: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
          children: [
            _ConnectionPill(
              connected: s.connected,
              demo: device.usingDemo,
              updated: s.lastUpdated,
            ).animate().fadeIn(),
            const SizedBox(height: 12),
            GlassCard(
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 28,
                    backgroundColor: s.raining
                        ? AppColors.rain.withValues(alpha: 0.2)
                        : AppColors.warning.withValues(alpha: 0.15),
                    child: WeatherLottie(raining: s.raining),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          s.raining ? 'Raining now' : 'No rain on site',
                          style: const TextStyle(fontWeight: FontWeight.w700),
                        ),
                        Text(
                          s.rainLikely24h
                              ? '⚠️ Rain likely in the next 24 hours.'
                              : 'Clear skies expected in the next 24 hours.',
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: GlassCard(
                    tint: s.raining ? AppColors.rain.withValues(alpha: 0.12) : null,
                    child: Row(
                      children: [
                        Icon(
                          Icons.water_drop,
                          color: s.raining ? AppColors.rain : AppColors.teal,
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Rain: ${Formatters.rain(s.raining)}',
                            style: const TextStyle(fontWeight: FontWeight.w600),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: GlassCard(
                    tint: s.tankFull
                        ? AppColors.tankFull.withValues(alpha: 0.12)
                        : AppColors.teal.withValues(alpha: 0.08),
                    child: Row(
                      children: [
                        Icon(
                          s.tankFull
                              ? Icons.warning_amber
                              : Icons.opacity,
                          color: s.tankFull
                              ? AppColors.tankFull
                              : AppColors.teal,
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Tank: ${Formatters.tank(s.tankFull)}',
                            style: const TextStyle(fontWeight: FontWeight.w600),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: 10,
              crossAxisSpacing: 10,
              childAspectRatio: 1.15,
              children: [
                SensorTile(
                  label: 'Temperature',
                  value: Formatters.temperature(
                    r.temperatureC,
                    useCelsius: settings.useCelsius,
                  ),
                  icon: Icons.thermostat,
                  color: AppColors.warning,
                  spark: series((e) => e.temperatureC),
                  error: r.temperatureFault,
                ),
                SensorTile(
                  label: 'Soil moisture',
                  value: Formatters.percent(r.soilMoisture),
                  icon: Icons.grass,
                  color: AppColors.teal,
                  spark: series((e) => e.soilMoisture),
                ),
                SensorTile(
                  label: 'Battery',
                  value: Formatters.percent(r.batteryPercent),
                  icon: Icons.battery_full,
                  color: AppColors.success,
                  spark: series((e) => e.batteryPercent),
                ),
                SensorTile(
                  label: 'Voltage',
                  value: Formatters.voltage(r.voltage),
                  icon: Icons.bolt,
                  color: AppColors.emerald,
                  spark: series((e) => e.voltage),
                ),
              ],
            ),
            const SizedBox(height: 16),
            MotorCard(
              title: 'Motor 1 · Field pump',
              subtitle: s.motor1On ? 'ON 🟢' : 'OFF 🔴',
              isOn: s.motor1On,
              locked: s.motor1Locked,
              lockIcon: Icons.umbrella,
              lockReason:
                  'Locked while raining. Field pump auto-turns-off when rain starts.',
              onToggle: (v) =>
                  ref.read(deviceControllerProvider.notifier).toggleMotor(1, v),
              onOpen: () => context.push('/motor/1'),
            ),
            const SizedBox(height: 10),
            MotorCard(
              title: 'Motor 2 · Tank pump',
              subtitle: s.motor2On ? 'ON 🟢' : 'OFF 🔴',
              isOn: s.motor2On,
              locked: s.motor2Locked,
              lockIcon: Icons.water,
              lockReason:
                  'Locked while tank is full. Auto-off when full, auto-on when empty.',
              onToggle: (v) =>
                  ref.read(deviceControllerProvider.notifier).toggleMotor(2, v),
              onOpen: () => context.push('/motor/2'),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Text(
                  'Recent alerts',
                  style: Theme.of(context)
                      .textTheme
                      .titleMedium
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
                const Spacer(),
                TextButton(
                  onPressed: () => context.go('/alerts'),
                  child: const Text('See all'),
                ),
              ],
            ),
            if (device.alerts.isEmpty)
              const GlassCard(child: Text('No alerts yet. System is quiet.')),
            ...device.alerts.take(4).map(
                  (a) => Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: GlassCard(
                      tint: a.isSecurity
                          ? AppColors.danger.withValues(alpha: 0.12)
                          : null,
                      onTap: () => context.go('/alerts'),
                      child: Row(
                        children: [
                          Text(a.icon, style: const TextStyle(fontSize: 22)),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  a.title,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                                Text(
                                  a.body,
                                  maxLines: 2,
                                  overflow: TextOverflow.ellipsis,
                                  style: Theme.of(context).textTheme.bodySmall,
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
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.14),
          borderRadius: BorderRadius.circular(99),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 8,
              height: 8,
              decoration: BoxDecoration(color: color, shape: BoxShape.circle),
            ),
            const SizedBox(width: 8),
            Text(
              '$label · ${Formatters.time.format(updated)}',
              style: TextStyle(
                color: color,
                fontWeight: FontWeight.w600,
                fontSize: 12,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
