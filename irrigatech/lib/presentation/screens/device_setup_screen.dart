import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';
import '../../domain/auth_controller.dart';
import '../../domain/device_controller.dart';
import '../../domain/settings_controller.dart';
import '../widgets/glass_card.dart';

class DeviceSetupScreen extends ConsumerStatefulWidget {
  const DeviceSetupScreen({super.key});

  @override
  ConsumerState<DeviceSetupScreen> createState() => _DeviceSetupScreenState();
}

class _DeviceSetupScreenState extends ConsumerState<DeviceSetupScreen> {
  final _controller = TextEditingController();
  bool _busy = false;
  String? _message;
  bool _ok = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _test() async {
    final token = _controller.text.trim();
    if (token.isEmpty) {
      setState(() {
        _ok = false;
        _message = 'Paste the Blynk auth token from the EcoSense device.';
      });
      return;
    }
    setState(() {
      _busy = true;
      _message = null;
    });
    final ok =
        await ref.read(deviceControllerProvider.notifier).testToken(token);
    setState(() {
      _busy = false;
      _ok = ok;
      _message = ok
          ? 'Hardware reachable on Blynk Cloud.'
          : 'Could not reach Blynk. Check the token, or stay in Demo mode.';
    });
  }

  Future<void> _save({required bool live}) async {
    final settings = ref.read(settingsControllerProvider.notifier);
    if (live) {
      final token = _controller.text.trim();
      if (token.isEmpty) return;
      await settings.saveToken(token);
      await settings.setDemoMode(false);
      await ref.read(authControllerProvider.notifier).linkDevice(token);
    } else {
      await settings.setDemoMode(true);
    }
    await ref.read(deviceControllerProvider.notifier).rebuildSource();
    if (mounted) context.go('/home');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Device Setup')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const GlassCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Link the irrigation controller',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
                ),
                SizedBox(height: 8),
                Text(
                  'This is independent from Google Sign-In. Paste the Blynk auth token for template Ecosense (TMPL6lSiWFFsO).',
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _controller,
            decoration: const InputDecoration(
              labelText: 'Blynk auth token',
              hintText: 'Paste token',
            ),
            obscureText: true,
          ),
          const SizedBox(height: 12),
          OutlinedButton(
            onPressed: _busy ? null : _test,
            child: Text(_busy ? 'Testing…' : 'Test connection'),
          ),
          if (_message != null) ...[
            const SizedBox(height: 12),
            Text(
              _message!,
              style: TextStyle(
                color: _ok ? AppColors.success : AppColors.warning,
              ),
            ),
          ],
          const SizedBox(height: 20),
          FilledButton(
            onPressed: () => _save(live: true),
            child: const Text('Save token & go live'),
          ),
          const SizedBox(height: 8),
          TextButton(
            onPressed: () => _save(live: false),
            child: const Text('Continue with Demo data'),
          ),
        ],
      ),
    );
  }
}
