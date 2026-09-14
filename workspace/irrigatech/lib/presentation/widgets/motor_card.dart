import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import 'glass_card.dart';
import 'status_icon.dart';

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
    final statusColor = isOn ? AppColors.success : scheme.outline;
    return GlassCard(
      onTap: onOpen,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              StatusIcon(
                icon: Icons.power_settings_new,
                color: statusColor,
              ),
              const SizedBox(width: AppSpace.md),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: Theme.of(context).textTheme.titleSmall),
                    const SizedBox(height: 2),
                    Text(
                      subtitle,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: statusColor,
                            fontWeight: FontWeight.w600,
                          ),
                    ),
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
            const SizedBox(height: AppSpace.md),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: AppColors.warning.withOpacity(0.08),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(lockIcon, size: 18, color: AppColors.warning),
                  const SizedBox(width: AppSpace.sm),
                  Expanded(
                    child: Text(
                      lockReason,
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}
