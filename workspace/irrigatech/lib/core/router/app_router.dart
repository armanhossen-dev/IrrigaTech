import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../domain/auth_controller.dart';
import '../../domain/settings_controller.dart';
import '../../presentation/screens/alerts_screen.dart';
import '../../presentation/screens/analytics_screen.dart';
import '../../presentation/screens/dashboard_screen.dart';
import '../../presentation/screens/device_setup_screen.dart';
import '../../presentation/screens/login_screen.dart';
import '../../presentation/screens/motor_detail_screen.dart';
import '../../presentation/screens/onboarding_screen.dart';
import '../../presentation/screens/security_log_screen.dart';
import '../../presentation/screens/settings_screen.dart';
import '../../presentation/screens/shell_screen.dart';
import '../../presentation/screens/splash_screen.dart';

final _rootKey = GlobalKey<NavigatorState>();
final _shellKey = GlobalKey<NavigatorState>();

final appRouterProvider = Provider<GoRouter>((ref) {
  final auth = ref.watch(authControllerProvider);
  final settings = ref.watch(settingsControllerProvider);

  return GoRouter(
    navigatorKey: _rootKey,
    initialLocation: '/splash',
    redirect: (context, state) {
      final loc = state.matchedLocation;
      final splashing = loc == '/splash';
      if (splashing) return null;

      if (!settings.onboardingDone && loc != '/onboarding') {
        return '/onboarding';
      }
      if (settings.onboardingDone && loc == '/onboarding') {
        return auth.signedIn ? '/home' : '/login';
      }
      if (!auth.signedIn && loc != '/login' && loc != '/onboarding') {
        return '/login';
      }
      if (auth.signedIn && loc == '/login') {
        return settings.hasBlynkToken || settings.useDemoMode
            ? '/home'
            : '/setup';
      }
      return null;
    },
    routes: [
      GoRoute(
        path: '/splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: '/onboarding',
        builder: (context, state) => const OnboardingScreen(),
      ),
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/setup',
        builder: (context, state) => const DeviceSetupScreen(),
      ),
      GoRoute(
        path: '/motor/:id',
        builder: (context, state) {
          final id = int.tryParse(state.pathParameters['id'] ?? '1') ?? 1;
          return MotorDetailScreen(motorIndex: id);
        },
      ),
      GoRoute(
        path: '/security',
        builder: (context, state) => const SecurityLogScreen(),
      ),
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) {
          return ShellScreen(navigationShell: navigationShell);
        },
        branches: [
          StatefulShellBranch(
            navigatorKey: _shellKey,
            routes: [
              GoRoute(
                path: '/home',
                builder: (context, state) => const DashboardScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/analytics',
                builder: (context, state) => const AnalyticsScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/alerts',
                builder: (context, state) => const AlertsScreen(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/settings',
                builder: (context, state) => const SettingsScreen(),
              ),
            ],
          ),
        ],
      ),
    ],
  );
});
