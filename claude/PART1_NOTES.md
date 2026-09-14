# IrrigaTech — PART 1 (Foundation) Notes

Handoff notes for the next Claude session (PART 2), which has no memory
of this one. Read this before writing any new code.

## What was built (fully functional)

- **Project scaffold**: `pubspec.yaml` with pinned versions of every
  required package (riverpod, go_router, google_fonts, dio,
  flutter_secure_storage, hive, firebase_*, fl_chart, etc.).
- **Theme**: `lib/core/theme.dart` — Material 3 Light + Dark ThemeData,
  teal (`AppTheme.seedTeal` = `#0F9B8E`) → emerald (`AppTheme.emerald` =
  `#10B981`) palette, Poppins via `google_fonts`, rounded corners (16–20px
  radius), soft-shadow cards. `main.dart` uses `ThemeMode.system`
  (auto light/dark); a manual override toggle is **not yet built** — that
  belongs in the Settings screen (PART 3).
- **Constants**: `lib/core/constants.dart` — app name/tagline, Blynk
  template ID/name, the full V0–V5 virtual-pin mapping, secure-storage
  key, demo-stream interval. **Always reference these constants**, don't
  hardcode pin strings or the app name again.
- **Data models** (`lib/data/models/`): `SensorData`, `MotorState`,
  `AlertLog` (+ `AlertSeverity` enum), `WeatherForecast`. All plain Dart
  classes with `fromJson`/`toJson`/`copyWith`. No `freezed`, no codegen —
  keep it that way unless you add the build_runner step project-wide.
- **Repository layer** (`lib/data/repositories/`):
  - `IrrigationRepository` — abstract interface. **All new data sources
    must implement this.** Methods: `sensorDataStream`, `motorStateStream`,
    `setMotorState(MotorId, bool)`, `testConnection()`, `getAlertLogs()`,
    `getWeatherForecast()`, `dispose()`.
  - `DemoDataRepository` — fully offline, streams gently-drifting fake
    sensor values every `AppConstants.demoStreamInterval` (3s). Always
    "connects" successfully. Returns 2 sample `AlertLog`s and a fixed
    `WeatherForecast`.
  - `BlynkRepository` — real `dio` calls to the two REST endpoints
    (`GET .../get?token=...&V0`, `GET .../update?token=...&V2=1`), polling
    every 5s (no live push API on free Blynk tier). Auth token is passed
    in via constructor, **never hardcoded**. `getAlertLogs()` and
    `getWeatherForecast()` are stubs here — real implementations are
    PART 2/3 work (Firestore + OpenWeatherMap respectively).
  - `AuthRepository` — wraps `firebase_auth` + `google_sign_in`.
    `signInWithGoogle()`, `signOut()`, `authStateChanges` stream,
    `currentUser` getter.
- **Riverpod providers** (`lib/domain/providers/repository_providers.dart`):
  - `repositoryModeProvider` (`StateProvider<RepositoryMode>`, enum
    `{demo, live}`, **defaults to `demo`**).
  - `blynkAuthTokenProvider` (`StateProvider<String>`, in-memory token).
  - `irrigationRepositoryProvider` — resolves to `DemoDataRepository` or
    `BlynkRepository` based on the two providers above. **UI code should
    only ever watch/read this provider**, typed as `IrrigationRepository`
    — never import `DemoDataRepository`/`BlynkRepository` directly in a
    screen.
  - `sensorDataStreamProvider`, `motorStateStreamProvider` — `StreamProvider`s
    built on top of `irrigationRepositoryProvider`. **PART 2's Dashboard
    screen should consume these**, not build its own repository access.
  - `authRepositoryProvider`, `authStateChangesProvider`.
- **Routing** (`lib/core/router.dart`): `AppRoutes` class holds every
  path as a constant (`splash`, `onboarding`, `login`, `deviceSetup`,
  `dashboard`, `motorDetail`, `analytics`, `alerts`, `securityLog`,
  `settings`). `goRouterProvider` wires all 10 routes. **Always add new
  routes here and reference `AppRoutes.xxx`** — don't hardcode path
  strings or build ad hoc `MaterialPageRoute`s.
- **Fully built screens** (`lib/presentation/screens/`):
  - `splash_screen.dart` — fade+scale logo animation (built-in
    `AnimationController`, no Lottie asset needed), auto-navigates to
    Onboarding after ~2.2s.
  - `onboarding_screen.dart` — 3-slide `PageView` (remote control /
    sensors / safety automation), dot indicator, "Skip" and
    "Next"/"Get Started" buttons, navigates to Login.
  - `login_screen.dart` — Google-branded sign-in button UI, calls
    `AuthRepository.signInWithGoogle()` via `authRepositoryProvider`,
    navigates to Device Setup on success, shows a `SnackBar` on failure
    (expected until `flutterfire configure` is run).
  - `device_setup_screen.dart` — Blynk token `TextField`, "Test
    Connection" button that flips `repositoryModeProvider` to `live` and
    calls `testConnection()` on the real `BlynkRepository`, success/error
    banner, persists the token via `flutter_secure_storage` under
    `AppConstants.secureStorageBlynkTokenKey` on success, reverts to Demo
    mode on failure, "Skip for now" link to stay in Demo mode.
- **Placeholder screens** (Scaffold + centered `Text(screenName)` only —
  **PART 2/3 build these**): `dashboard_screen.dart`,
  `motor_detail_screen.dart`, `analytics_screen.dart`,
  `alerts_screen.dart`, `security_log_screen.dart`, `settings_screen.dart`.
  Each already has a doc-comment stub explaining what it should become —
  read those comments first.
- **`main.dart`**: `ProviderScope` → `MaterialApp.router`, Firebase init
  wrapped in try/catch (won't crash before `flutterfire configure` is
  run), `ThemeMode.system`.
- **`lib/firebase_options.dart`**: placeholder that throws
  `UnsupportedError` if actually used. **Must be regenerated** by running
  `flutterfire configure` before Firebase Auth will work end-to-end.

## What's still a placeholder / explicitly NOT built

- Dashboard, Motor control, Analytics, Alerts, Security Log, and Settings
  screen **contents** (only empty Scaffolds exist) — this is PART 2/3.
- Manual Light/Dark theme toggle (currently `ThemeMode.system` only).
- Real `flutterfire configure` output (current `firebase_options.dart` is
  a stub).
- OpenWeatherMap integration (`BlynkRepository.getWeatherForecast()` is a
  fixed placeholder value).
- Firestore-backed alert/security history (`BlynkRepository.getAlertLogs()`
  returns `[]`).
- Telegram bot integration — not referenced anywhere yet.
- App icons/launcher config (`flutter_launcher_icons` is in `pubspec.yaml`
  but no `assets/icons/app_icon.png` exists yet — add the image before
  running `flutter pub run flutter_launcher_icons`).
- Push notifications wiring (`flutter_local_notifications` +
  `firebase_messaging` are dependencies only, not yet used in code).

## Exact names PART 2 needs to extend

| Concept | File | Symbol |
|---|---|---|
| Repository interface | `lib/data/repositories/irrigation_repository.dart` | `IrrigationRepository` |
| Demo implementation | `lib/data/repositories/demo_data_repository.dart` | `DemoDataRepository` |
| Live implementation | `lib/data/repositories/blynk_repository.dart` | `BlynkRepository` |
| Auth wrapper | `lib/data/repositories/auth_repository.dart` | `AuthRepository` |
| Mode switch provider | `lib/domain/providers/repository_providers.dart` | `repositoryModeProvider`, `RepositoryMode` |
| Repository provider | `lib/domain/providers/repository_providers.dart` | `irrigationRepositoryProvider` |
| Sensor stream provider | `lib/domain/providers/repository_providers.dart` | `sensorDataStreamProvider` |
| Motor stream provider | `lib/domain/providers/repository_providers.dart` | `motorStateStreamProvider` |
| Route constants | `lib/core/router.dart` | `AppRoutes` |
| Router provider | `lib/core/router.dart` | `goRouterProvider` |
| Theme | `lib/core/theme.dart` | `AppTheme.lightTheme`, `AppTheme.darkTheme` |
| Constants | `lib/core/constants.dart` | `AppConstants` |
| Models | `lib/data/models/*.dart` | `SensorData`, `MotorState`, `MotorId`, `AlertLog`, `AlertSeverity`, `WeatherForecast` |
| Screen to flesh out | `lib/presentation/screens/dashboard_screen.dart` | `DashboardScreen` |
| Screen to flesh out | `lib/presentation/screens/motor_detail_screen.dart` | `MotorDetailScreen` |
| Screen to flesh out | `lib/presentation/screens/analytics_screen.dart` | `AnalyticsScreen` |
| Screen to flesh out | `lib/presentation/screens/alerts_screen.dart` | `AlertsScreen` |
| Screen to flesh out | `lib/presentation/screens/security_log_screen.dart` | `SecurityLogScreen` |
| Screen to flesh out | `lib/presentation/screens/settings_screen.dart` | `SettingsScreen` |

## Setup steps before running

1. `flutter pub get`
2. `dart pub global activate flutterfire_cli` then `flutterfire configure`
   to replace `lib/firebase_options.dart` with real values.
3. Add a real `assets/icons/app_icon.png` if you want launcher icons
   generated.
4. Run — the app boots straight into Demo mode with no token needed.


