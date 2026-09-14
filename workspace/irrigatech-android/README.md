# IrrigaTech - Android App

Smart irrigation companion for Android. Monitors soil moisture, temperature,
battery and tank level, and controls two pumps through the Blynk IoT Cloud.

**Save Water | Save Life**

## Features

- Splash -> Google Sign-In -> Connection Setup -> Dashboard flow (demo profile when Firebase is not yet configured).
- Live dashboard polling every 5-30 seconds, paused automatically in the background, with pull-to-refresh.
- Sensor cards: temperature (V0), soil moisture (V1), battery (V4), voltage (V5).
- Two motors: Field motor (V2) and Tank motor (V3) with a confirmation dialog before starting.
- Safety interlocks: field motor blocked while raining, tank motor blocked while the tank is full.
- Temperature sentinel handling: a `-127.0` reading shows a "Sensor error" state instead of a blank value.
- Alerts history (motor / rain / tank / battery / security / system) with filters and clear.
- Settings: Light / Dark / System theme, three accent swatches, polling interval, C / F units, notification toggles, controller management, About + sign out.
- Background alert checks through WorkManager so safety alerts arrive even when the app is closed.
- Blynk tokens are stored with `EncryptedSharedPreferences` and are never hardcoded.
- Offline banner with retry and last-known values, plus a demo data source when no controller is configured.

## Tech Stack

Kotlin, Jetpack Compose, Material 3, MVVM (ViewModel + StateFlow), Navigation Compose,
Retrofit + OkHttp + kotlinx.serialization, DataStore, EncryptedSharedPreferences,
Firebase Auth (Google) + Firestore, WorkManager, Coil, Coroutines/Flow.

- minSdk 24, targetSdk/compileSdk 35, Java 17.
- No DI framework: a small hand-written `AppContainer` keeps the APK small.

## Project Structure

```
app/src/main/java/com/ahrn/irrigatech/
  IrrigaTechApp.kt            Application + container bootstrap
  MainActivity.kt             Compose entry point + theme
  data/
    model/Models.kt           Domain models
    remote/                   Blynk REST + demo data source
    local/                    Encrypted token store, DataStore device/settings/session stores
    repository/               Sensor + alert repositories, alert rules
    auth/                     Firebase Google Sign-In with demo fallback
  di/AppContainer.kt          Manual dependency graph
  notifications/              Notification channels + WorkManager worker
  ui/
    navigation/               Routes + NavHost
    theme/                    Colors, typography, shapes, Material 3 theme
    components/               Shared Compose components
    util/                     Formatting helpers
    viewmodel/                ViewModels per screen
    screens/                  splash, login, setup, dashboard, motor, alerts, settings, about
```

## Getting Started

1. Open the project folder in Android Studio (Ladybug or newer).
2. Let Gradle sync. The project builds without Firebase thanks to a conditional
   Google Services plugin and a demo auth/data fallback.
3. Run on a device or emulator (API 24+).

### Enable real Firebase Google Sign-In

1. Create a Firebase project (suggested id: `irrigatech-a3927`).
2. Add an Android app with package name `com.ahrn.irrigatech` and register your
   debug and release SHA-1 / SHA-256 fingerprints.
3. Enable **Authentication -> Sign-in method -> Google**.
4. Download `google-services.json` and place it at `app/google-services.json`.
5. Copy the **Web client ID** (Authentication -> Google -> Web SDK configuration)
   into `google_web_client_id` in `app/src/main/res/values/strings.xml`.
6. Rebuild. The app now signs in with Google and stores devices per user.

### Connect a real controller

In the app, open Connection Setup and enter:

- Device name
- Blynk Auth Token (masked, stored encrypted)
- Template ID (defaults to `TMPL6lSiWFFsO`)
- Farm location and optional Telegram bot

Use **Test Connection** to ping Blynk cloud before saving. Multiple controllers
can be saved and switched between.

## Blynk Pin Map

| Pin | Meaning         |
|-----|-----------------|
| V0  | Temperature     |
| V1  | Soil moisture   |
| V2  | Field motor M1  |
| V3  | Tank motor M2   |
| V4  | Battery         |
| V5  | Voltage         |

Read: `GET https://blynk.cloud/external/api/get?token={TOKEN}&V0`
Write: `GET https://blynk.cloud/external/api/update?token={TOKEN}&V2=1`

## Notes

- The project does not include a real `google-services.json` or any credentials.
- `gradlew` and the wrapper jar are included, so `./gradlew assembleDebug` works
  once a JDK 17 and the Android SDK are installed.
