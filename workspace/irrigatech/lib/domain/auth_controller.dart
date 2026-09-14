import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/models/app_user.dart';
import '../data/services/auth_service.dart';
import 'settings_controller.dart';

class AuthState {
  const AuthState({
    required this.signedIn,
    this.user,
    this.busy = false,
    this.error,
    this.firebaseReady = false,
  });

  final bool signedIn;
  final AppUser? user;
  final bool busy;
  final String? error;
  final bool firebaseReady;
}

class AuthController extends StateNotifier<AuthState> {
  AuthController(this._auth, this._settings)
      : super(const AuthState(signedIn: false));

  final AuthService _auth;
  final SettingsController _settings;

  Future<void> bootstrap() async {
    await _auth.attachFirebase();
    final firebaseUser = _auth.currentUser;
    final demo = _settings.state.demoSignedIn;
    if (firebaseUser != null) {
      state = AuthState(
        signedIn: true,
        firebaseReady: _auth.firebaseReady,
        user: AppUser(
          uid: firebaseUser.uid,
          displayName: firebaseUser.displayName,
          email: firebaseUser.email,
          photoUrl: firebaseUser.photoURL,
        ),
      );
    } else if (demo) {
      state = AuthState(
        signedIn: true,
        firebaseReady: _auth.firebaseReady,
        user: const AppUser(
          uid: 'demo-user',
          displayName: 'Demo Farmer',
          email: 'demo@irrigatech.local',
        ),
      );
    } else {
      state = AuthState(signedIn: false, firebaseReady: _auth.firebaseReady);
    }
  }

  Future<void> signInGoogle() async {
    state = AuthState(
      signedIn: state.signedIn,
      user: state.user,
      busy: true,
      firebaseReady: state.firebaseReady,
    );
    try {
      final user = await _auth.signInWithGoogle();
      await _settings.setDemoSignedIn(false);
      state = AuthState(
        signedIn: true,
        user: user,
        firebaseReady: true,
      );
    } catch (e) {
      state = AuthState(
        signedIn: false,
        firebaseReady: _auth.firebaseReady,
        error: e.toString(),
      );
    }
  }

  /// Lets reviewers use the full app before Firebase files are added.
  Future<void> continueAsDemo() async {
    await _settings.setDemoSignedIn(true);
    state = const AuthState(
      signedIn: true,
      firebaseReady: false,
      user: AppUser(
        uid: 'demo-user',
        displayName: 'Demo Farmer',
        email: 'demo@irrigatech.local',
      ),
    );
  }

  Future<void> signOut() async {
    await _auth.signOut();
    await _settings.setDemoSignedIn(false);
    state = AuthState(signedIn: false, firebaseReady: _auth.firebaseReady);
  }

  Future<void> linkDevice(String token) => _auth.linkDeviceToken(token);

  Stream<User?> firebaseAuthState() => _auth.authState();
}

final authServiceProvider = Provider<AuthService>((ref) => AuthService());

final authControllerProvider =
    StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(
    ref.watch(authServiceProvider),
    ref.watch(settingsControllerProvider.notifier),
  );
});
