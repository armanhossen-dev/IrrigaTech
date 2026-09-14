import 'package:flutter/material.dart';

import '../../core/theme/app_theme.dart';

class StatusIcon extends StatelessWidget {
  const StatusIcon({
    super.key,
    required this.icon,
    required this.color,
    this.size = AppSpace.icon,
  });

  final IconData icon;
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 36,
      height: 36,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Icon(icon, size: size, color: color),
    );
  }
}
