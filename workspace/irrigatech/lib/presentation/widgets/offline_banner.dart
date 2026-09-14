import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../domain/device_controller.dart';

class OfflineBanner extends ConsumerWidget {
  const OfflineBanner({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final status = ref.watch(deviceControllerProvider).status;
    if (status.connected && status.lastError == null) {
      return const SizedBox.shrink();
    }
    return Material(
      color: AppColors.warning.withOpacity(0.12),
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: AppSpace.lg,
            vertical: AppSpace.sm,
          ),
          child: Row(
            children: [
              const Icon(
                Icons.cloud_off_outlined,
                color: AppColors.warning,
                size: AppSpace.icon,
              ),
              const SizedBox(width: AppSpace.sm),
              Expanded(
                child: Text(
                  status.lastError ??
                      'Blynk API is unreachable. Showing last known values.',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: AppColors.warning,
                      ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
