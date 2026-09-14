import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';
import '../../core/theme/status_visuals.dart';
import '../../core/utils/formatters.dart';
import '../../data/models/alert_event.dart';
import '../utils/alert_copy.dart';
import 'glass_card.dart';
import 'status_icon.dart';

class AlertTile extends StatelessWidget {
  const AlertTile({
    super.key,
    required this.event,
    this.compact = false,
    this.onTap,
  });

  final AlertEvent event;
  final bool compact;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final color = StatusVisuals.colorFor(event.type, event.severity);
    final icon = StatusVisuals.iconFor(event.type);
    final theme = Theme.of(context);
    return GlassCard(
      tint: event.isSecurity ? AppColors.danger.withOpacity(0.08) : null,
      onTap: onTap,
      padding: const EdgeInsets.all(AppSpace.lg),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          StatusIcon(icon: icon, color: color),
          const SizedBox(width: AppSpace.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  AlertCopy.title(event),
                  style: theme.textTheme.titleSmall,
                ),
                const SizedBox(height: 4),
                Text(
                  AlertCopy.body(event),
                  maxLines: compact ? 2 : 4,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodySmall,
                ),
                if (!compact) ...[
                  const SizedBox(height: 6),
                  Text(
                    Formatters.dateTime.format(event.timestamp),
                    style: theme.textTheme.labelSmall,
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }
}
