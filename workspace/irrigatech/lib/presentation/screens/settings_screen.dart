import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/constants/app_constants.dart';
import '../../core/theme/app_theme.dart';
import '../../domain/auth_controller.dart';
import '../../domain/device_controller.dart';
import '../../domain/settings_controller.dart';
import '../widgets/glass_card.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final settings = ref.watch(settingsControllerProvider);
    final ctrl = ref.read(settingsControllerProvider.notifier);
    final auth = ref.watch(authControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: ListView(
        padding: const EdgeInsets.all(AppSpace.lg),
        children: [
          GlassCard(
            child: ListTile(
              contentPadding: EdgeInsets.zero,
              leading: CircleAvatar(
                backgroundImage: auth.user?.photoUrl != null
                    ? NetworkImage(auth.user!.photoUrl!)
                    : null,
                child: auth.user?.photoUrl == null
                    ? const Icon(Icons.person_outline, size: AppSpace.iconLg)
                    : null,
              ),
              title: Text(auth.user?.displayName ?? 'Operator'),
              subtitle: Text(auth.user?.email ?? ''),
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          const _Section('Appearance'),
          GlassCard(
            child: Column(
              children: [
                RadioListTile<ThemeMode>(
                  title: const Text('System'),
                  value: ThemeMode.system,
                  groupValue: settings.themeMode,
                  onChanged: (v) => ctrl.setThemeMode(v!),
                ),
                RadioListTile<ThemeMode>(
                  title: const Text('Light'),
                  value: ThemeMode.light,
                  groupValue: settings.themeMode,
                  onChanged: (v) => ctrl.setThemeMode(v!),
                ),
                RadioListTile<ThemeMode>(
                  title: const Text('Dark'),
                  value: ThemeMode.dark,
                  groupValue: settings.themeMode,
                  onChanged: (v) => ctrl.setThemeMode(v!),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          const _Section('Data source'),
          GlassCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Demo mode'),
                  subtitle: const Text(
                    'Fake streaming sensors. Turn off to use live Blynk REST.',
                  ),
                  value: settings.useDemoMode,
                  onChanged: (v) async {
                    await ctrl.setDemoMode(v);
                    await ref
                        .read(deviceControllerProvider.notifier)
                        .rebuildSource();
                  },
                ),
                ListTile(
                  title: const Text('Blynk token'),
                  subtitle: Text(
                    settings.hasBlynkToken ? 'Token saved on device' : 'Not set',
                  ),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => context.push('/setup'),
                ),
                ListTile(
                  title: const Text('Polling interval'),
                  subtitle: Text('${settings.pollInterval.inSeconds} seconds'),
                ),
                Slider(
                  min: 2,
                  max: 10,
                  divisions: 8,
                  value: settings.pollInterval.inSeconds.toDouble(),
                  label: '${settings.pollInterval.inSeconds}s',
                  onChanged: (v) async {
                    await ctrl.setPollSeconds(v.round());
                    await ref
                        .read(deviceControllerProvider.notifier)
                        .rebuildSource();
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          const _Section('Units & notifications'),
          GlassCard(
            child: Column(
              children: [
                SwitchListTile(
                  title: const Text('Celsius'),
                  subtitle: const Text('Off = Fahrenheit'),
                  value: settings.useCelsius,
                  onChanged: ctrl.setCelsius,
                ),
                SwitchListTile(
                  title: const Text('Tank alerts'),
                  value: settings.notifyTank,
                  onChanged: (v) => ctrl.setNotify(tank: v),
                ),
                SwitchListTile(
                  title: const Text('Rain alerts'),
                  value: settings.notifyRain,
                  onChanged: (v) => ctrl.setNotify(rain: v),
                ),
                SwitchListTile(
                  title: const Text('Tamper alerts'),
                  value: settings.notifyTamper,
                  onChanged: (v) => ctrl.setNotify(tamper: v),
                ),
                SwitchListTile(
                  title: const Text('Low battery alerts'),
                  value: settings.notifyBattery,
                  onChanged: (v) => ctrl.setNotify(battery: v),
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          const _Section('Account'),
          GlassCard(
            child: Column(
              children: [
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.logout, size: AppSpace.iconLg),
                  title: const Text('Sign out'),
                  onTap: () async {
                    await ref.read(authControllerProvider.notifier).signOut();
                    if (context.mounted) context.go('/login');
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: AppSpace.lg),
          const _Section('About'),
          GlassCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  AppConstants.appName,
                  style: Theme.of(context).textTheme.titleSmall,
                ),
                Text(
                  AppConstants.tagline,
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                const SizedBox(height: AppSpace.sm),
                Text(
                  'Version 1.0.0  ·  ${AppConstants.credit}',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                const SizedBox(height: AppSpace.sm),
                Text(
                  'Blynk template: Ecosense (TMPL6lSiWFFsO). Pins V0–V5.',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Section extends StatelessWidget {
  const _Section(this.label);
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8, left: 4),
      child: Text(
        label,
        style: Theme.of(context).textTheme.titleSmall,
      ),
    );
  }
}
