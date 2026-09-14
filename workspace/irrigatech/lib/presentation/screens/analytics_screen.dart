import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../data/models/sensor_reading.dart';
import '../../domain/device_controller.dart';
import '../widgets/glass_card.dart';

class AnalyticsScreen extends ConsumerStatefulWidget {
  const AnalyticsScreen({super.key});

  @override
  ConsumerState<AnalyticsScreen> createState() => _AnalyticsScreenState();
}

class _AnalyticsScreenState extends ConsumerState<AnalyticsScreen> {
  int _rangeHours = 24;

  @override
  Widget build(BuildContext context) {
    final device = ref.watch(deviceControllerProvider);
    final from = DateTime.now().subtract(Duration(hours: _rangeHours));
    final points =
        device.history.where((r) => r.timestamp.isAfter(from)).toList();
    final runtime = _motorRuntimeHours(device, from);

    return Scaffold(
      appBar: AppBar(title: const Text('Analytics')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpace.lg),
        children: [
          SegmentedButton<int>(
            segments: const [
              ButtonSegment(value: 24, label: Text('24h')),
              ButtonSegment(value: 168, label: Text('7d')),
              ButtonSegment(value: 720, label: Text('30d')),
            ],
            selected: {_rangeHours},
            onSelectionChanged: (s) => setState(() => _rangeHours = s.first),
          ),
          const SizedBox(height: AppSpace.lg),
          Row(
            children: [
              Expanded(
                child: GlassCard(
                  child: _Stat(
                    label: 'Motor 1 runtime',
                    value: '${runtime.$1.toStringAsFixed(1)} h',
                  ),
                ),
              ),
              const SizedBox(width: AppSpace.md),
              Expanded(
                child: GlassCard(
                  child: _Stat(
                    label: 'Motor 2 runtime',
                    value: '${runtime.$2.toStringAsFixed(1)} h',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpace.lg),
          _ChartCard(
            title: 'Temperature (°C)',
            color: AppColors.warning,
            points: points,
            pick: (r) => r.temperatureFault ? 0 : r.temperatureC,
          ),
          const SizedBox(height: AppSpace.md),
          _ChartCard(
            title: 'Soil moisture (%)',
            color: AppColors.teal,
            points: points,
            pick: (r) => r.soilMoisture,
          ),
          const SizedBox(height: AppSpace.md),
          _ChartCard(
            title: 'Voltage (V)',
            color: AppColors.info,
            points: points,
            pick: (r) => r.voltage,
          ),
        ],
      ),
    );
  }

  (double, double) _motorRuntimeHours(dynamic device, DateTime from) {
    final hist = device.motorHistory as List;
    if (hist.isEmpty) return (0, 0);
    var m1 = 0.0;
    var m2 = 0.0;
    DateTime cursor = from;
    bool on1 = false;
    bool on2 = false;
    for (final snap in hist) {
      if (snap.changedAt.isBefore(from)) {
        on1 = snap.motor1On;
        on2 = snap.motor2On;
        continue;
      }
      final dt = snap.changedAt.difference(cursor).inSeconds / 3600.0;
      if (on1) m1 += dt;
      if (on2) m2 += dt;
      on1 = snap.motor1On;
      on2 = snap.motor2On;
      cursor = snap.changedAt;
    }
    final tail = DateTime.now().difference(cursor).inSeconds / 3600.0;
    if (on1) m1 += tail;
    if (on2) m2 += tail;
    return (m1, m2);
  }
}

class _Stat extends StatelessWidget {
  const _Stat({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: Theme.of(context).textTheme.bodySmall),
        Text(value, style: Theme.of(context).textTheme.titleMedium),
      ],
    );
  }
}

class _ChartCard extends StatelessWidget {
  const _ChartCard({
    required this.title,
    required this.color,
    required this.points,
    required this.pick,
  });

  final String title;
  final Color color;
  final List<SensorReading> points;
  final double Function(SensorReading) pick;

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: AppSpace.md),
          SizedBox(
            height: 180,
            child: points.length < 2
                ? const Center(child: Text('Collecting samples…'))
                : LineChart(
                    LineChartData(
                      gridData: FlGridData(
                        show: true,
                        drawVerticalLine: false,
                        getDrawingHorizontalLine: (v) => FlLine(
                          color: Theme.of(context).dividerColor.withOpacity(0.4),
                          strokeWidth: 1,
                        ),
                      ),
                      titlesData: const FlTitlesData(
                        topTitles: AxisTitles(
                          sideTitles: SideTitles(showTitles: false),
                        ),
                        rightTitles: AxisTitles(
                          sideTitles: SideTitles(showTitles: false),
                        ),
                      ),
                      borderData: FlBorderData(show: false),
                      lineBarsData: [
                        LineChartBarData(
                          isCurved: true,
                          color: color,
                          barWidth: 2.4,
                          dotData: const FlDotData(show: false),
                          spots: [
                            for (var i = 0; i < points.length; i++)
                              FlSpot(i.toDouble(), pick(points[i])),
                          ],
                          belowBarData: BarAreaData(
                            show: true,
                            color: color.withOpacity(0.12),
                          ),
                        ),
                      ],
                    ),
                  ),
          ),
        ],
      ),
    );
  }
}
