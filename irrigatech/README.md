# IrrigaTech

**Smart Irrigation. Effortless Farming.**

Flutter companion app for the EcoSense / IrrigaTech ESP32-S3 + Arduino Uno irrigation system. It talks to Blynk Cloud over REST, signs users in with Google (Firebase Auth), and ships with a fully working **Demo mode** so you can explore every screen before hardware or Firebase is configured.

Package name: `com.ahrn.irrigatech`  
Version: `1.0.0`  
Built by Arman Hossen (AHRN)

---

## Screens

1. Animated splash (logo reveal)
2. Onboarding (3 slides, first run)
3. Login — Google Sign-In + Continue in demo mode
4. Device Setup — paste Blynk token, test connection, or stay in Demo
5. Dashboard — connection pill, weather/rain card, sensor tiles with sparklines, rain + tank indicators, Motor 1 / Motor 2 cards with interlock locks, recent alerts
6. Motor detail — large toggle, interlock copy, on/off history
7. Analytics — 24h / 7d / 30d charts (temp, moisture, voltage) + motor runtime totals
8. Alerts — chronological EcoSense-style feed, filters, pull-to-refresh, full status report sheet
9. Security log — keypad wrong-attempt counter and tamper timestamps
10. Settings — theme, notifications, polling, Blynk token, Demo/Live switch, units, sign-out, about

Offline / Blynk-unreachable state is an **app-wide banner**, not a blocking screen.

---

## Prerequisites

- Flutter stable 3.22+ (Dart 3.4+)
- Android Studio / Xcode command-line tools
- A Blynk auth token from the EcoSense device (for Live mode)
- A Firebase project (for real Google Sign-In; Demo mode works without it)

```bash
flutter pub get
flutterfire configure
flutter run
```

Generate launcher icons after `pub get`:

```bash
dart run flutter_launcher_icons
```

---

## Firebase console checklist

These steps must be done in the Firebase console and on your machine. The generated app cannot finish them for you.

1. Create a Firebase project at console.firebase.google.com.
2. Add an Android app with package name `com.ahrn.irrigatech` → download `google-services.json` → place in `android/app/`.
3. Add debug and release SHA-1/SHA-256 fingerprints (`./gradlew signingReport`) in the Firebase Android app settings — Google Sign-In fails without this.
4. (Optional iOS) Add iOS app + bundle ID → `GoogleService-Info.plist` → `ios/Runner/`.
5. Authentication → Sign-in method → enable Google, set support email.
6. Firestore Database → create in production mode → apply the per-user security rules snippet (see below).
7. Confirm Cloud Messaging is enabled (no extra config needed).
8. Run `flutterfire configure` at project root to generate `lib/firebase_options.dart`.
9. `flutter clean && flutter pub get && flutter run`.

Until those files exist, use **Continue in demo mode** on the login screen. Auth (Google) and the device link (Blynk token) are independent — never conflated.

### Firestore rules snippet

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      match /alerts/{alertId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /history/{docId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

The same rules live in `firestore.rules`.

On sign-in the app upserts `users/{uid}` with `displayName`, `email`, `photoUrl`, `linkedDeviceToken`, `fcmToken`. Alerts and history are stored per-user in Firestore, with Hive as the offline cache.

---

## Blynk token setup

Hardware: ESP32-S3 + Arduino Uno, Blynk template **Ecosense**, Template ID `TMPL6lSiWFFsO`.

Paste the device **Auth Token** in Device Setup (or Settings → Blynk token). It is stored in `flutter_secure_storage`, not in Firebase Auth.

### REST endpoints

```
GET https://blynk.cloud/external/api/get?token={TOKEN}&V0
GET https://blynk.cloud/external/api/update?token={TOKEN}&V2=1
```

The app polls every 2–3 seconds while the dashboard is in the foreground and pauses in the background.

### Virtual pins

| Pin | Meaning | Type |
| --- | --- | --- |
| V0 | Temperature (°C) | read |
| V1 | Soil moisture (%) | read |
| V2 | Motor 1 (Field pump) ON/OFF | read/write |
| V3 | Motor 2 (Tank pump) ON/OFF | read/write |
| V4 | Battery % | read |
| V5 | Voltage (V) | read |

Firmware sentinel `-127.0` on V0 is shown as **Sensor Error**, not a blank card.

### Safety interlocks (UI)

- Motor 1 toggle is locked (rain icon + explanation) while raining; auto-off when rain starts.
- Motor 2 toggle is locked (tank-full icon + explanation) while the tank is full; auto-off when full, auto-on when empty.
- 3+ wrong keypad codes raise a red 🚨 tamper card in Alerts and the Security log.

---

## Demo vs Live

| Mode | Source | When |
| --- | --- | --- |
| Demo (default) | `DemoDataRepository` — streaming fake-but-realistic sensors, rain/tank/tamper cycles | First run, or Settings → Demo mode ON |
| Live | `BlynkRepository` REST against your token | Settings → Demo mode OFF + saved token |

Both implement `IrrigationRepository`. Switch them in Settings without changing screens.

---

## Architecture

```
lib/
  core/          theme, constants, formatters, go_router
  data/          models, Demo + Blynk repositories, Hive store, auth, notifications
  domain/        settings / auth / device controllers (Riverpod)
  presentation/  screens + glass cards, motor cards, charts
```

- State: **Riverpod**
- Navigation: **go_router** with auth-redirect guards
- Local history: **Hive**
- Charts: **fl_chart**
- Fonts: **google_fonts** (Poppins)
- Motion: **flutter_animate** + Lottie asset

---

## Run instructions

```bash
cd irrigatech
flutter pub get
dart run flutter_launcher_icons
flutterfire configure
flutter run
```

Android signing report (for Firebase SHA fingerprints):

```bash
cd android
./gradlew signingReport
```

---

## EcoSense alert language

Alerts and the full-status bottom sheet follow the Telegram bot field order:

- 🔋 Battery charge
- ⚡ Voltage
- 🌡️ Temperature
- 💧 Soil moisture
- 🌧️ Current rain — None / Raining
- 🚰 Tank status — Empty 💧 / Full 🛑
- 🔌 Tank motor — ON 🟢 / OFF 🔴
- 🔮 Forecast footer — Clear skies expected in the next 24 hours. / ⚠️ Rain likely in the next 24 hours.

Security cards use 🚨 on a red-tinted tile. Boot copy: `✅ System is online and booted successfully!`

---

## Known limitations

- `google-services.json`, `GoogleService-Info.plist`, and real `firebase_options.dart` values are **user-supplied**. Placeholders are in the repo so Demo mode still boots.
- OpenWeatherMap is not called directly; 24h rain likelihood is taken from the device/demo stream (the firmware already uses OWM).
- FCM data messages require a real Firebase project; local notifications still fire in Demo mode for tank / rain / tamper / low battery.
- iOS Google Sign-In needs `GIDClientID` and the reversed client-id URL scheme filled from your plist after `flutterfire configure`.
- If platform folders look incomplete on a fresh machine, run `flutter create . --project-name irrigatech --org com.ahrn` inside this directory, then restore `android/app/build.gradle` applicationId `com.ahrn.irrigatech`.

---

Built by Arman Hossen (AHRN)
