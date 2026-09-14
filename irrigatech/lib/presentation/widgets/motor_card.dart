import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

import '../../core/theme/app_theme.dart';
import 'glass_card.dart';

class MotorCard extends StatelessWidget {
  const MotorCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.isOn,
    required this.locked,
    required this.lockReason,
    required this.lockIcon,
    required this.onToggle,
    required this.onOpen,
  });

  final String title;
  final String subtitle;
  final bool isOn;
  final bool locked;
  final String lockReason;
  final IconData lockIcon;
  final ValueChanged<bool> onToggle;
  final VoidCallback onOpen;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return GlassCard(
      onTap: onOpen,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                backgroundColor: isOn
                    ? AppColors.success.withValues(alpha: 0.18)
                    : scheme.surfaceContainerHighest,
                child: Icon(
                  Icons.power_settings_new,
                  color: isOn ? AppColors.success : scheme.outline,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title,
                        style: const TextStyle(fontWeight: FontWeight.w600)),
                    Text(subtitle,
                        style: Theme.of(context).textTheme.bodySmall),
                  ],
                ),
              ),
              Switch.adaptive(
                value: isOn,
                onChanged: locked ? null : onToggle,
              ),
            ],
          ),
          if (locked) ...[
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: AppColors.warning.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Icon(lockIcon, size: 18, color: AppColors.warning),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      lockReason,
                      style: const TextStyle(fontSize: 12),
                    ),
                  ),
                ],
              ),
            ).animate().fadeIn(),
          ],
        ],
      ),
    );
  }
}
