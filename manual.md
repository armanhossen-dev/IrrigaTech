# IrrigaTech User Manual

Welcome to **IrrigaTech**, your smart irrigation companion. This manual provides instructions on how to set up, monitor, and control your irrigation system using the IrrigaTech Android app.

---

## 1. Getting Started

### Installation
Ensure you have the IrrigaTech app installed on your Android device (Android 7.0 / API 24 or higher).

### First Launch & Sign In
- **Sign In**: Use your Google account to sign in. This allows you to sync your farm data and settings securely.
- **Demo Mode**: If you don't have a hardware controller yet, you can explore the app's features using built-in mock data.

### Connecting a Controller
To control real hardware:
1. Navigate to **Settings** > **Manage connections**.
2. Enter your **Blynk Auth Token** (found in your Blynk console).
3. The app will securely store this token and start polling data from your sensors.

---

## 2. Dashboard Overview

 The Dashboard is your central hub for real-time farm data.

- **Weather Hero**: Displays current temperature, humidity, wind speed, and weather condition at your farm.
- **Soil Moisture**: A gauge showing the moisture percentage. It helps you decide when to irrigate.
- **Water Tank**: Monitors your borewell feed tank level.
- **Metrics Grid**:
    - **Soil Temp**: Current temperature of the soil.
    - **System Voltage**: Monitors the power supply/battery health of your controller.
    - **Rain Sensor**: Indicates if rain is currently detected at the site.

---

## 3. Controlling Your Pumps

IrrigaTech allows you to remotely toggle two types of pumps:

### Field Irrigation Pump
- Used for primary crop irrigation (e.g., Drip line - Zone A).
- **Safety Feature**: The app will automatically block the Field Pump if the rain sensor is active to save water.

### Tank Pump
- Used for Borewell feed to fill your water tank.
- **Safety Feature**: The app will automatically block the Tank Pump when the tank is full to prevent overflow.

### Security (Keypad Unlock)
To prevent accidental activation:
1. Tap the toggle on a pump card.
2. If the keypad is locked, enter your **Security PIN** (Default is `1234`).
3. Confirm the action in the dialog box.

---

## 4. Alerts & Notifications

Stay informed about your farm's status even when the app is closed.

- **Notifications**: You will receive alerts for critical events:
    - Tank full.
    - Rain detected (and pump paused).
    - Low battery (below 20%).
    - Automatic motor stops.
- **Alert History**: Tap the notification icon in the top bar to view a history of all system events.

---

## 5. Customizing the App

Navigate to **Settings** to tailor the experience:

### Appearance
- **Theme**: Switch between Light, Dark, or System default modes.
- **Accent Colour**: Choose a color swatch that matches your style.

### Data & Units
- **Refresh Rate**: Adjust how often the app polls data from your controller (1 to 30 seconds).
- **Temperature Unit**: Switch between **Celsius (°C)** and **Fahrenheit (°F)**. This affects all temperature displays across the app.

---

## 6. Troubleshooting

- **Offline Status**: If the dashboard shows "Offline," check your controller's internet connection and ensure your Auth Token is correct.
- **Sensor Faults**: If a metric shows `--`, it may indicate a wiring issue or a faulty sensor at the farm.
- **Security Reset**: If you forget your PIN, please contact your farm administrator or reset the app data (requires re-connection of controllers).

---

**Save Water | Save Life**
*IrrigaTech Team*
