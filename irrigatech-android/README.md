# IrrigaTech - Smart Irrigation Companion

**Save Water | Save Life**

IrrigaTech is a modern Android application designed for efficient agricultural water management. It serves as a smart monitoring and control hub, allowing farmers to oversee soil conditions, temperature, and water levels while remotely controlling irrigation pumps through the Blynk IoT cloud.

---

## 🚀 Key Features

- **Live Dashboard**: Real-time polling (5–30s) of sensors including Temperature (V0), Soil Moisture (V1), Battery (V4), and Voltage (V5).
- **Intelligent Motor Control**: Independent control for Field and Tank motors with safety interlocks (e.g., blocking field motor when raining, tank motor when full).
- **Safety Sentinels**: Built-in logic to handle sensor errors (e.g., `-127.0` temperature reading) and automated background safety checks via WorkManager.
- **Alert History**: Categorized logs for motor, rain, tank, battery, security, and system events with smart filtering.
- **Customizable Experience**: Supports Light/Dark/System themes, accent swatches, configurable polling intervals, and unit preferences (Celsius/Fahrenheit).
- **Secure by Design**: Encrypted storage for Blynk tokens and session data; Google Sign-In for user authentication.
- **Demo Mode**: Built-in mock data source allows exploring the app features even without a live controller.

---

## 🛠️ Tech Stack & Architecture

IrrigaTech is built using modern Android development practices, emphasizing performance and a small APK footprint.

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Networking**: Retrofit + OkHttp + kotlinx.serialization
- **Data Storage**: 
  - **DataStore**: For app settings and non-sensitive session data.
  - **EncryptedSharedPreferences**: For secure storage of Blynk Auth Tokens.
- **Dependency Injection**: Manual `AppContainer` pattern (no Hilt/Dagger to keep the project lightweight).
- **Background Tasks**: WorkManager for persistent safety monitoring.
- **Images**: Coil for efficient image loading.
- **Backend**: Firebase Auth (Google), Firestore, and Blynk IoT Cloud REST API.

---

## 📂 Project Structure

```text
app/src/main/java/com/ahrn/irrigatech/
├── data/
│   ├── auth/         # Firebase Google Sign-In logic
│   ├── local/        # DataStore & Encrypted Token storage
│   ├── model/        # Domain models
│   ├── remote/       # Blynk REST & Demo mock sources
│   └── repository/   # Sensor, Alert, and Feedback repositories
├── di/               # Manual dependency graph (AppContainer)
├── notifications/    # Channels & WorkManager background workers
├── ui/
│   ├── components/   # Reusable UI building blocks
│   ├── navigation/   # Compose routes & NavHost
│   ├── screens/      # Feature screens (Dashboard, Settings, Alerts, etc.)
│   ├── theme/        # M3 Color schemes, Typography, and Spacing
│   ├── util/         # String & Date formatting helpers
│   └── viewmodel/    # Screen-level StateFlow management
└── IrrigaTechApp.kt  # Application class & Container bootstrap
```

---

## ⚙️ Setup Instructions

### 1. Prerequisites
- Android Studio **Ladybug** or newer.
- JDK 17.
- A Blynk IoT Cloud account and a configured template.

### 2. Getting Started
1. Clone the repository and open it in Android Studio.
2. Let Gradle sync and download dependencies.
3. The app is configured to build in **Demo Mode** by default. You can run it immediately on an emulator or device (API 24+).

### 3. Real Firebase Integration
To enable Google Sign-In and cloud storage:
1. Create a project on the [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package name `com.ahrn.irrigatech`.
3. Register your debug/release SHA fingerprints.
4. Download `google-services.json` and place it in the `app/` folder.
5. Copy the **Web client ID** from the Google provider in Firebase Auth and paste it into `google_web_client_id` in `app/src/main/res/values/strings.xml`.

### 4. Controller Configuration
Once in the app, navigate to **Connection Setup** to link your hardware:
- **Auth Token**: Your Blynk device token (stored securely).
- **Template ID**: Defaults to `TMPL6lSiWFFsO`.
- **Farm Details**: Optional location and Telegram bot integration.

---

## 👥 Project Team

The IrriGaTech project is a collaborative effort combining expertise across multiple disciplines:

| Member | Department | Role |
|---|---|---|
| **Md. Arman Hossen Ripon** | Computer Science & Engineering | **Lead Developer & System Integration** |
| **Md. Shoaib Bin Yousuf** | Electrical & Electronic Engineering | **IoT & Hardware Integration** |
| **Md. Al Mozahid Monir** | Agricultural Science | **Agricultural & Crop Management** |

*Special thanks to the collaborative spirit of agricultural science and engineering.*

---

## 📄 License & Notes

- **Save Water | Save Life** — A mission for a sustainable future.
- This project is a production-ready template for IoT irrigation systems.
- For issues or feedback, please visit [AHRN Portfolio](https://www.armanhossen.is-a.dev/).
