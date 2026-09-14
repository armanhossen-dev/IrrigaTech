import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/constants/app_constants.dart';
import '../../core/theme/app_theme.dart';
import '../../domain/auth_controller.dart';
import '../../domain/device_controller.dart';
import '../../domain/settings_controller.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _boot();
  }

  Future<void> _boot() async {
    await ref.read(settingsControllerProvider.notifier).load();
    await ref.read(authControllerProvider.notifier).bootstrap();
    await ref.read(deviceControllerProvider.notifier).start();
    await Future<void>.delayed(const Duration(milliseconds: 1600));
    if (!mounted) return;
    final settings = ref.read(settingsControllerProvider);
    final auth = ref.read(authControllerProvider);
    if (!settings.onboardingDone) {
      context.go('/onboarding');
    } else if (!auth.signedIn) {
      context.go('/login');
    } else if (!settings.hasBlynkToken && !settings.useDemoMode) {
      context.go('/setup');
    } else {
      context.go('/home');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [AppColors.deepTeal, AppColors.teal, AppColors.emerald],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 72,
                height: 72,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white.withOpacity(0.12),
                ),
                child: const Icon(
                  Icons.water_drop_outlined,
                  size: 32,
                  color: Colors.white,
                ),
              )
                  .animate()
                  .scale(duration: 500.ms, curve: Curves.easeOut)
                  .fadeIn(),
              const SizedBox(height: AppSpace.lg),
              Text(
                AppConstants.appName,
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      color: Colors.white,
                    ),
              ).animate().fadeIn(delay: 250.ms),
              const SizedBox(height: 6),
              Text(
                AppConstants.tagline,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: Colors.white.withOpacity(0.9),
                    ),
              ).animate().fadeIn(delay: 500.ms),
            ],
          ),
        ),
      ),
    );
  }
}
