import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../core/utils/formatters.dart';
import '../../data/models/alert_event.dart';
import '../../domain/device_controller.dart';
import '../widgets/glass_card.dart';
import '../widgets/status_report_sheet.dart';

class AlertsScreen extends ConsumerStatefulWidget {
  const AlertsScreen({super.key});

  @override
  ConsumerState<AlertsScreen> createState() => _AlertsScreenState();
}

class _AlertsScreenState extends ConsumerState<AlertsScreen> {
  AlertType? _filter;

  @override
  Widget build(BuildContext context) {
    final alerts = ref.watch(deviceControllerProvider).alerts;
    final filtered =
        _filter == null ? alerts : alerts.where((a) => a.type == _filter).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Alerts'),
        actions: [
          IconButton(
            tooltip: 'Full EcoSense report',
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
        ],
      ),
      body: Column(
        children: [
          SizedBox(
            height: 44,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 12),
              children: [
                _chip('All', null),
                _chip('Rain', AlertType.rain),
                _chip('Tank', AlertType.tankFull),
                _chip('Tamper', AlertType.tamper),
                _chip('Boot', AlertType.boot),
                _chip('Battery', AlertType.lowBattery),
              ],
            ),
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () =>
                  ref.read(deviceControllerProvider.notifier).refresh(),
              child: filtered.isEmpty
                  ? ListView(
                      children: const [
                        SizedBox(height: 80),
                        Center(child: Text('No matching alerts')),
                      ],
                    )
                  : ListView.builder(
                      padding: const EdgeInsets.all(16),
                      itemCount: filtered.length,
                      itemBuilder: (context, i) {
                        final a = filtered[i];
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 10),
                          child: GlassCard(
                            tint: a.isSecurity
                                ? AppColors.danger.withValues(alpha: 0.14)
                                : a.severity == AlertSeverity.success
                                    ? AppColors.success.withValues(alpha: 0.08)
                                    : null,
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(a.icon,
                                    style: const TextStyle(fontSize: 26)),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        a.title,
                                        style: const TextStyle(
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      const SizedBox(height: 4),
                                      Text(a.body),
                                      const SizedBox(height: 6),
                                      Text(
                                        Formatters.dateTime.format(a.timestamp),
                                        style: Theme.of(context)
                                            .textTheme
                                            .bodySmall,
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _chip(String label, AlertType? type) {
    final selected = _filter == type;
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: FilterChip(
        label: Text(label),
        selected: selected,
        onSelected: (_) => setState(() => _filter = type),
      ),
    );
  }
}
