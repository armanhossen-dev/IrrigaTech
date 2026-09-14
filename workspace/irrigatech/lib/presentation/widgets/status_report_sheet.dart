import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/theme/app_theme.dart';
import '../../core/theme/status_visuals.dart';
import '../../core/utils/formatters.dart';
import '../../data/models/device_status.dart';
import 'glass_card.dart';
import 'status_icon.dart';

class StatusReportSheet extends StatelessWidget {
  const StatusReportSheet({super.key, required this.status});

  final DeviceStatus status;

  @override
  Widget build(BuildContext context) {
    final r = status.reading;
    final theme = Theme.of(context);
    final forecast = status.rainLikely24h
        ? 'Rain likely in the next 24 hours.'
        : 'Clear skies expected in the next 24 hours.';
    final rows = <_ReportRow>[
      _ReportRow(
        icon: StatusVisuals.battery,
        color: r.batteryPercent <= 20 ? AppColors.warning : AppColors.success,
        label: 'Battery charge',
        value: Formatters.percent(r.batteryPercent),
      ),
      _ReportRow(
        icon: StatusVisuals.voltage,
        color: AppColors.info,
        label: 'Voltage',
        value: Formatters.voltage(r.voltage),
      ),
      _ReportRow(
        icon: StatusVisuals.temperature,
        color: r.temperatureFault ? AppColors.danger : AppColors.warning,
        label: 'Temperature',
        value: r.temperatureFault
            ? 'Sensor error'
            : '${r.temperatureC.toStringAsFixed(1)}°C',
      ),
      _ReportRow(
        icon: StatusVisuals.moisture,
        color: AppColors.teal,
        label: 'Soil moisture',
        value: Formatters.percent(r.soilMoisture),
      ),
      _ReportRow(
        icon: StatusVisuals.rain,
        color: status.raining ? AppColors.rain : AppColors.success,
        label: 'Current rain',
        value: Formatters.rain(status.raining),
      ),
      _ReportRow(
        icon: StatusVisuals.tank,
        color: status.tankFull ? AppColors.danger : AppColors.teal,
        label: 'Tank status',
        value: status.tankFull ? 'Full' : 'Empty',
      ),
      _ReportRow(
        icon: StatusVisuals.motor,
        color: status.motor2On ? AppColors.success : AppColors.danger,
        label: 'Tank motor',
        value: status.motor2On ? 'On' : 'Off',
      ),
      _ReportRow(
        icon: StatusVisuals.forecast,
        color: status.rainLikely24h ? AppColors.warning : AppColors.info,
        label: 'Forecast',
        value: forecast,
      ),
    ];

    final plain = rows.map((e) => '${e.label} — ${e.value}').join('\n');

    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 36,
            height: 4,
            decoration: BoxDecoration(
              color: Theme.of(context).dividerColor,
              borderRadius: BorderRadius.circular(99),
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          Text('System status', style: theme.textTheme.titleMedium),
          const SizedBox(height: AppSpace.md),
          GlassCard(
            child: Column(
              children: [
                for (var i = 0; i < rows.length; i++) ...[
                  if (i > 0)
                    Divider(
                      height: 20,
                      color: Theme.of(context).dividerColor,
                    ),
                  Row(
                    children: [
                      StatusIcon(icon: rows[i].icon, color: rows[i].color),
                      const SizedBox(width: AppSpace.md),
                      Expanded(
                        child: Text(
                          rows[i].label,
                          style: theme.textTheme.bodySmall,
                        ),
                      ),
                      Flexible(
                        child: Text(
                          rows[i].value,
                          textAlign: TextAlign.end,
                          style: theme.textTheme.titleSmall,
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: AppSpace.md),
          FilledButton.icon(
            onPressed: () {
              Clipboard.setData(ClipboardData(text: 'EcoSense Report\n$plain'));
              Navigator.of(context).pop();
            },
            icon: const Icon(Icons.copy, size: AppSpace.icon),
            label: const Text('Copy report'),
          ),
        ],
      ),
    );
  }
}

class _ReportRow {
  const _ReportRow({
    required this.icon,
    required this.color,
    required this.label,
    required this.value,
  });

  final IconData icon;
  final Color color;
  final String label;
  final String value;
}
