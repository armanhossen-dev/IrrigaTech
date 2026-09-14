import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/constants/app_constants.dart';
import '../../core/theme/app_theme.dart';
import '../../domain/auth_controller.dart';
import '../../domain/settings_controller.dart';
import '../widgets/glass_card.dart';

class LoginScreen extends ConsumerWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);
    ref.listen(authControllerProvider, (prev, next) {
      if (next.signedIn) {
        final settings = ref.read(settingsControllerProvider);
        context.go(
          settings.hasBlynkToken || settings.useDemoMode ? '/home' : '/setup',
        );
      }
    });

    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [
              AppColors.mist.withOpacity(0.9),
              Theme.of(context).scaffoldBackgroundColor,
            ],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              children: [
                const Spacer(),
                const Icon(Icons.spa, size: 64, color: AppColors.teal)
                    .animate()
                    .fadeIn()
                    .scale(),
                const SizedBox(height: 12),
                Text(
                  AppConstants.appName,
                  style: Theme.of(context)
                      .textTheme
                      .headlineMedium
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
                Text(AppConstants.tagline),
                const SizedBox(height: 28),
                GlassCard(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const Text(
                        'Sign in',
                        style: TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 18,
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'Google Sign-In identifies you. The Blynk token is linked separately in Device Setup — the two are never mixed.',
                      ),
                      const SizedBox(height: 20),
                      FilledButton.icon(
                        onPressed: auth.busy
                            ? null
                            : () => ref
                                .read(authControllerProvider.notifier)
                                .signInGoogle(),
                        icon: const Icon(Icons.g_mobiledata, size: 28),
                        label: Text(
                          auth.busy ? 'Signing in…' : 'Sign in with Google',
                        ),
                      ),
                      const SizedBox(height: 12),
                      OutlinedButton(
                        onPressed: () => ref
                            .read(authControllerProvider.notifier)
                            .continueAsDemo(),
                        child: const Text('Continue in demo mode'),
                      ),
                      if (auth.error != null) ...[
                        const SizedBox(height: 12),
                        Text(
                          auth.error!,
                          style: const TextStyle(
                            color: AppColors.danger,
                            fontSize: 12,
                          ),
                        ),
                      ],
                      if (!auth.firebaseReady) ...[
                        const SizedBox(height: 12),
                        Text(
                          'Firebase files are not in this build yet. Use demo mode, then follow the README checklist to enable Google Sign-In.',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ],
                  ),
                ),
                const Spacer(),
                Text(
                  AppConstants.credit,
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
