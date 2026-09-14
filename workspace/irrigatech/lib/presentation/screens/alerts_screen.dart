import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../data/models/alert_event.dart';
import '../../domain/device_controller.dart';
import '../widgets/alert_tile.dart';
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
    final device = ref.watch(deviceControllerProvider);
    final alerts = device.alerts;
    final filtered =
        _filter == null ? alerts : alerts.where((a) => a.type == _filter).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Alerts'),
        actions: [
          IconButton(
            tooltip: 'System status',
            onPressed: () {
              showModalBottomSheet<void>(
                context: context,
                isScrollControlled: true,
                builder: (_) => StatusReportSheet(status: device.status),
              );
            },
            icon: const Icon(Icons.article_outlined, size: AppSpace.iconLg),
          ),
        ],
      ),
      body: Column(
        children: [
          SizedBox(
            height: 48,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: AppSpace.lg),
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
                      padding: const EdgeInsets.fromLTRB(
                        AppSpace.lg,
                        AppSpace.sm,
                        AppSpace.lg,
                        AppSpace.xl,
                      ),
                      itemCount: filtered.length,
                      itemBuilder: (context, i) {
                        return Padding(
                          padding: const EdgeInsets.only(bottom: AppSpace.md),
                          child: AlertTile(event: filtered[i]),
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
      padding: const EdgeInsets.only(right: AppSpace.sm),
      child: FilterChip(
        label: Text(label),
        selected: selected,
        visualDensity: VisualDensity.compact,
        onSelected: (_) => setState(() => _filter = type),
      ),
    );
  }
}
