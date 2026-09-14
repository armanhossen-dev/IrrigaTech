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
    return GlassCard(
      padding: const EdgeInsets.all(14),
      tint: error ? AppColors.danger.withOpacity(0.12) : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: error ? AppColors.danger : color, size: 18),
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
          const SizedBox(height: 8),
          Text(
            value,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w700,
                  color: error ? AppColors.danger : null,
                ),
          ),
          const Spacer(),
          Sparkline(values: spark, color: error ? AppColors.danger : color),
        ],
      ),
    );
  }
}
