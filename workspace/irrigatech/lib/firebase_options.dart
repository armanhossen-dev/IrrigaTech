// Generated placeholder. Replace by running `flutterfire configure`
// after adding google-services.json / GoogleService-Info.plist.
// See README.md section "Firebase console checklist".

import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      default:
        return android;
    }
  }

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'REPLACE_WITH_FIREBASE_ANDROID_API_KEY',
    appId: '1:000000000000:android:0000000000000000',
    messagingSenderId: '000000000000',
    projectId: 'irrigatech-REPLACE',
    storageBucket: 'irrigatech-REPLACE.appspot.com',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'REPLACE_WITH_FIREBASE_IOS_API_KEY',
    appId: '1:000000000000:ios:0000000000000000',
    messagingSenderId: '000000000000',
    projectId: 'irrigatech-REPLACE',
    storageBucket: 'irrigatech-REPLACE.appspot.com',
    iosBundleId: 'com.ahrn.irrigatech',
  );

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'REPLACE_WITH_FIREBASE_WEB_API_KEY',
    appId: '1:000000000000:web:0000000000000000',
    messagingSenderId: '000000000000',
    projectId: 'irrigatech-REPLACE',
    storageBucket: 'irrigatech-REPLACE.appspot.com',
  );
}
