import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/theme/app_theme.dart';
import 'glass_card.dart';

class StatusReportSheet extends StatelessWidget {
  const StatusReportSheet({super.key, required this.report});

  final String report;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: Colors.grey.withOpacity(0.4),
              borderRadius: BorderRadius.circular(99),
            ),
          ),
          const SizedBox(height: 16),
          const Text(
            'EcoSense Report',
            style: TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
          ),
          const SizedBox(height: 12),
          GlassCard(
            tint: AppColors.mist.withOpacity(0.35),
            child: SelectableText(
              report,
              style: const TextStyle(height: 1.55, fontSize: 14),
            ),
          ),
          const SizedBox(height: 12),
          FilledButton.icon(
            onPressed: () {
              Clipboard.setData(ClipboardData(text: report));
              Navigator.of(context).pop();
            },
            icon: const Icon(Icons.copy),
            label: const Text('Copy report'),
          ),
        ],
      ),
    );
  }
}
