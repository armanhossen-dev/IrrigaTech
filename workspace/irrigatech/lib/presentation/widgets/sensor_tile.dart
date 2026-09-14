import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import 'glass_card.dart';
import 'sparkline.dart';

class SensorTile extends StatelessWidget {
  const SensorTile({
    super.key,
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
    required this.spark,
    this.error = false,
  });

  final String label;
  final String value;
  final IconData icon;
  final Color color;
  final List<double> spark;
  final bool error;

  @override
  Widget build(BuildContext context) {
    final accent = error ? AppColors.danger : color;
    return GlassCard(
      padding: const EdgeInsets.all(AppSpace.md),
      tint: error ? AppColors.danger.withOpacity(0.06) : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: accent, size: AppSpace.icon),
              const SizedBox(width: 6),
              Expanded(
                child: Text(
                  label,
                  style: Theme.of(context).textTheme.bodySmall,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: AppSpace.sm),
          Text(
            value,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  color: error ? AppColors.danger : null,
                ),
          ),
          const Spacer(),
          Sparkline(values: spark, color: accent),
        ],
      ),
    );
  }
}
