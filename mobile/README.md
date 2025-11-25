# 🏓 Insane Cross-Mobile Ping Pong Controller

> **Turn your smartphone into a motion-controlled ping pong racket.**
> Play against friends on Android or iOS in a shared digital arena.

![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue?logo=kotlin)
![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-blue?logo=jetpackcompose)
![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![iOS](https://img.shields.io/badge/Platform-iOS-lightgrey?logo=apple)

---

## 📖 Overview

**Insane Cross-Mobile Ping Pong** is a distributed multiplayer game system. This repository contains the **Mobile Controller App**, which transforms your phone into a physical game controller.

Instead of tapping buttons, you **swing your phone** in the air. The app detects the motion, calculates the speed, and sends it to the game server in real-time. When you hit the ball, your phone vibrates and plays a sound, creating a tactile, immersive experience.

### ✨ Key Features

*   **📱 Real-Time Motion Control:** Uses the device's accelerometer to detect physical swings.
*   **🤝 Cross-Platform Play:** Android and iOS users can play against each other seamlessly.
*   **🔊 Immersive Feedback:** Instant haptic vibration and custom sound effects on "hit".
*   **🌍 Global Reach:** Play with friends anywhere in the world (low-latency WebSocket).
*   **🎨 Dynamic Theming:** Beautiful Dark/Light mode that adapts to your environment.
*   **🌐 Localization:** Automatic English and German language support.

---

## 🏗️ Architecture

The system is built on a **Kotlin Multiplatform (KMP)** foundation, sharing business logic across platforms while using native sensors for maximum performance.

---

## 🛠️ Tech Stack

*   **Language:** Kotlin (100%)
*   **UI Framework:** Compose Multiplatform (Material 3)
*   **Networking:** Ktor (WebSockets)
*   **Serialization:** Kotlinx Serialization (JSON)
*   **Sensors:**
    *   **Android:** `android.hardware.SensorManager`
    *   **iOS:** `CoreMotion` Framework
*   **Audio/Haptics:**
    *   **Android:** `MediaPlayer`, `Vibrator`
    *   **iOS:** `AVAudioPlayer`, `AudioServicesPlaySystemSound`

---

## 🚀 Getting Started

### Prerequisites
*   **JDK 17+**
*   **Android Studio** (for Android)
*   **Xcode** (for iOS)
*   **Mac** (required for iOS development)

### Running on Android
```bash
./gradlew composeApp:installDebug
```

### Running on iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run on a Simulator or Device.
*Or run via terminal:*
```bash
./gradlew composeApp:compileKotlinIosSimulatorArm64
```

---

## 🎮 How to Play

1.  **Launch the App:** Open the app on your phone.
2.  **Select Player:** Choose "Player 1" or "Player 2".
3.  **Connect:** The app will automatically connect to the game server.
4.  **Swing:** Hold your phone firmly (⚠️ **Use a wrist strap if possible!**) and swing it like a ping pong racket.
5.  **Feel the Hit:** When the virtual ball hits your paddle, your phone will vibrate and play a sound!

---

## 🐞 Debugging

Tap the **Ladybug Icon** 🐞 in the top-right corner of the Game Screen to open the Debug Overlay.
*   **Connection Status:** See real-time WebSocket state.
*   **Sensor Data:** View raw Accelerometer (X, Y, Z) values.
*   **Calibration:** Re-center the controller if it drifts.

---

## 📄 License

This project is created for **HackaTUM 2025**.
Code is available under the MIT License.