# GEMINI.md - Context for AI Assistants

This file provides comprehensive context for AI assistants interacting with the `MarauderController` project.

## 1. Project Overview

**Name:** ESP32 Marauder Controller
**Type:** Android Application (Native)
**Purpose:** A mobile command center for ESP32 Marauder devices, enabling WiFi analysis, scanning, and offensive security operations via USB OTG.
**Primary Technology:** Kotlin, Jetpack Compose, Android SDK (API 34).
**Architecture:** MVVM (Model-View-ViewModel), Clean Architecture principles.

### Key Capabilities
- **USB Serial Bridge:** Communicates with ESP32 firmware via USB OTG using `usb-serial-for-android`.
- **WiFi Reconnaissance:** Scan for Access Points (APs) and Stations (Clients).
- **Offensive Operations:** Execute deauth attacks, beacon spam, probe floods, and more.
- **Terminal Interface:** Direct command-line interaction with the ESP32.
- **Automation:** Scripting engine for automated attack/scan sequences.
- **Wardriving:** GPS integration for mapping WiFi networks (WiGLE format export).

## 2. Technical Stack & Dependencies

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Build System:** Gradle 8.6 with Kotlin DSL (`build.gradle.kts`)
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)
- **Key Libraries:**
    - `usb-serial-for-android` (Included as a local module)
    - `androidx.compose.*` (UI)
    - `androidx.lifecycle.*` (ViewModel, Lifecycle)
    - `kotlinx.coroutines` (Concurrency)
    - `kotlinx.serialization` (JSON parsing)
    - `androidx.datastore` (Preferences)

## 3. Directory Structure

```text
MarauderController/
├── app/                        # Main Android Application Module
│   ├── src/main/
│   │   ├── java/com/justcallmekoko/maraudercontroller/
│   │   │   ├── data/           # Data Layer (Repositories, Serial, Protocol)
│   │   │   │   ├── protocol/   # Parsing logic for ESP32 responses
│   │   │   │   ├── repository/ # Data management
│   │   │   │   └── serial/     # USB communication logic
│   │   │   ├── ui/             # UI Layer (Compose)
│   │   │   │   ├── components/ # Reusable UI elements (Terminal, etc.)
│   │   │   │   ├── screens/    # Main screens (WiFi, Attacks, Settings)
│   │   │   │   └── viewmodel/  # State management
│   │   │   ├── MainActivity.kt # Entry point
│   │   │   └── MarauderApplication.kt
│   │   └── res/                # Android Resources (Layouts, Strings, Values)
│   └── build.gradle.kts        # App-specific build config
├── usb-serial-for-android/     # Library module for serial communication
├── build.gradle.kts            # Root build config
├── settings.gradle.kts         # Project include settings
├── build.sh / build.bat        # Helper build scripts
├── README.md                   # Main documentation
├── QUICKSTART.md               # User guide
└── NEW_FEATURES.md             # Feature changelog
```

## 4. Build & Run Instructions

### Standard Build (Gradle)
The project uses the standard Gradle wrapper.

*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
    *Output:* `app/build/outputs/apk/debug/app-debug.apk`

*   **Build Release APK:**
    ```bash
    ./gradlew assembleRelease
    ```

*   **Clean Build:**
    ```bash
    ./gradlew clean
    ```

### Helper Scripts
Convenience scripts are provided for cross-platform building.
*   **Linux/Mac:** `./build.sh debug`
*   **Windows:** `build.bat debug`

### Installation
*   **Via ADB:** `adb install app/build/outputs/apk/debug/app-debug.apk`

## 5. Development Conventions

*   **Code Style:** Standard Kotlin conventions. Use `val` over `var` where possible.
*   **UI Pattern:** Jetpack Compose. Prefer functional components and State hoisting.
*   **Async:** Use Kotlin Coroutines (`suspend` functions, `viewModelScope`) and `Flow` for reactive data streams.
*   **Serial Communication:**
    *   **Core Protocol:** Uses a custom Binary Protocol (v1.1) for high-speed command and control (Scanning, Attacks, Files).
    *   **Legacy:** Some text-based output is still parsed for logging/debugging, but core logic relies on binary packets (`RESP_SCAN_DATA`).
    *   **Key Components:** `SerialConnectionManager` (handles transport & binary parsing), `MarauderBinaryProtocol` (schema definitions).
*   **Resources:** All strings should be extracted to `strings.xml` for potential future localization.

## 6. Key Configuration Files

*   `app/src/main/AndroidManifest.xml`: Defines permissions (USB_HOST, LOCATION, WIFI), activities, and USB device intent filters.
*   `app/src/main/res/xml/device_filter.xml` (Referenced in Manifest): Specifies supported USB Vendor IDs (VID) and Product IDs (PID) for the intent filter.

## 7. Operational Notes

*   **Permissions:** The app requires runtime permissions for Location (for WiFi/BLE scanning) and USB access.
*   **Hardware:** Requires an Android device with USB OTG support and an ESP32 Marauder device.
*   **Testing:** Since this relies on physical USB hardware, standard emulators are limited. Logic verification should rely on unit tests, while hardware integration often requires physical device testing.
