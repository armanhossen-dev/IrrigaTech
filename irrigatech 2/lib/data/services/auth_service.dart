import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../core/constants/app_constants.dart';
import '../models/app_user.dart';

/// Firebase Auth (Google) is independent from the Blynk device token.
class AuthService {
  AuthService({
    FirebaseAuth? auth,
    GoogleSignIn? googleSignIn,
    FirebaseFirestore? firestore,
  })  : _auth = auth,
        _google = googleSignIn ?? GoogleSignIn(scopes: const ['email', 'profile']),
        _firestore = firestore;

  FirebaseAuth? _auth;
  final GoogleSignIn _google;
  FirebaseFirestore? _firestore;
  bool firebaseReady = false;

  Future<void> attachFirebase() async {
    try {
      if (Firebase.apps.isEmpty) {
        await Firebase.initializeApp();
      }
      _auth = FirebaseAuth.instance;
      _firestore = FirebaseFirestore.instance;
      firebaseReady = true;
    } catch (_) {
      firebaseReady = false;
    }
  }

  Stream<User?> authState() {
    if (_auth == null) return Stream<User?>.value(null);
    return _auth!.authStateChanges();
  }

  User? get currentUser => _auth?.currentUser;

  Future<AppUser> signInWithGoogle() async {
    if (!firebaseReady || _auth == null) {
      throw StateError(
        'Firebase is not configured. Add google-services.json and run flutterfire configure.',
      );
    }
    final googleUser = await _google.signIn();
    if (googleUser == null) {
      throw StateError('Sign-in cancelled');
    }
    final googleAuth = await googleUser.authentication;
    final credential = GoogleAuthProvider.credential(
      accessToken: googleAuth.accessToken,
      idToken: googleAuth.idToken,
    );
    final result = await _auth!.signInWithCredential(credential);
    final user = result.user!;
    String? fcm;
    try {
      fcm = await FirebaseMessaging.instance.getToken();
    } catch (_) {}
    final appUser = AppUser(
      uid: user.uid,
      displayName: user.displayName,
      email: user.email,
      photoUrl: user.photoURL,
      fcmToken: fcm,
    );
    await upsertUser(appUser);
    return appUser;
  }

  /// Upsert users/{uid} — never stores the Blynk token as the user identity.
  Future<void> upsertUser(AppUser user) async {
    if (_firestore == null) return;
    await _firestore!.collection('users').doc(user.uid).set(
          user.toFirestore(),
          SetOptions(merge: true),
        );
  }

  Future<void> linkDeviceToken(String token) async {
    final uid = currentUser?.uid;
    if (uid == null || _firestore == null) return;
    await _firestore!.collection('users').doc(uid).set(
      {'linkedDeviceToken': token, 'updatedAt': DateTime.now().toIso8601String()},
      SetOptions(merge: true),
    );
  }

  Future<void> persistAlert(Map<String, dynamic> alert) async {
    final uid = currentUser?.uid;
    if (uid == null || _firestore == null) return;
    await _firestore!
        .collection('users')
        .doc(uid)
        .collection('alerts')
        .doc(alert['id'] as String)
        .set(alert);
  }

  Future<void> persistReading(Map<String, dynamic> reading) async {
    final uid = currentUser?.uid;
    if (uid == null || _firestore == null) return;
    await _firestore!
        .collection('users')
        .doc(uid)
        .collection('history')
        .add(reading);
  }

  Future<void> signOut() async {
    try {
      await _google.signOut();
    } catch (_) {}
    try {
      await _auth?.signOut();
    } catch (_) {}
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(AppConstants.prefsDemoSignedIn, false);
  }
}
