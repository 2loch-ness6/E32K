# ESP32 Marauder Project Context

## Project Overview
The **ESP32 Marauder** is a suite of offensive and defensive WiFi/Bluetooth tools designed for the ESP32 platform. This repository contains the source code for the ESP32 firmware, hardware design files, and a companion Android application.

### Key Components
1.  **Firmware (`esp32_marauder/`):** The core C++/Arduino software that runs on the ESP32 hardware. It handles WiFi/Bluetooth scanning, attacks, and serial communication.
2.  **Android App (`MarauderController/`):** A native Android application (Kotlin/Jetpack Compose) that acts as a GUI controller for the Marauder device via USB OTG serial connection.
3.  **Hardware (`PCBs/`, `mechanical/`):** Design files for various hardware iterations of the Marauder board.

---

## 1. Firmware (`esp32_marauder/`)

### Technologies
*   **Platform:** ESP32 (Arduino Framework)
*   **Language:** C++
*   **Key Libraries:** `WiFi`, `BLEDevice`, `SD`, `TFT_eSPI` (for display), `NeoPixel` (for LEDs).

### Structure
*   `esp32_marauder.ino`: Main entry point (setup and loop).
*   `configs.h`: **CRITICAL**. Defines the hardware target (e.g., `MARAUDER_FLIPPER`, `MARAUDER_M5STICKC`). You *must* check this file to see which board is currently selected for build.
*   `CommandLine.cpp/h`: Handles the text-based CLI over serial.
*   `WiFiScan.cpp/h`: Core logic for WiFi scanning and attacks.
*   `EvilPortal.cpp/h`: Implementation of the captive portal attack.

### Building & Flashing
*   **Method:** Standard Arduino IDE or PlatformIO (though file structure favors Arduino IDE).
*   **Configuration:** Uncomment the `#define` for your specific hardware in `configs.h` before compiling.
*   **Libraries:** Requires installing specific libraries found in the `libraries/` directory or standard Arduino library manager.

---

## 2. Android Controller (`MarauderController/`)

### Technologies
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material Design 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Build System:** Gradle (Kotlin DSL)

### Key Files
*   `build.gradle.kts`: Project and app-level build configurations.
*   `app/src/main/java/.../serial/SerialConnectionManager.kt`: Manages USB OTG communication.
*   `app/src/main/java/.../protocol/MarauderProtocolParser.kt`: Parses the text/binary output from the ESP32.

### Building
Run the following from the `MarauderController` directory:
```bash
# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
*Output Location:* `app/build/outputs/apk/debug/app-debug.apk`

---

## 3. Development Conventions

*   **Firmware:**
    *   Use `#ifdef` macros in `configs.h` to manage code for different hardware targets.
    *   Serial communication is primarily text-based CLI (commands start with strings, responses often raw text), but a binary protocol is being introduced for efficiency.
*   **Android:**
    *   Follow modern Android architecture (Clean Architecture, Repositories, ViewModels).
    *   UI components should be composables.
    *   USB permissions are handled at runtime.

## 4. Operational Notes
*   **Serial Protocol:** The app communicates with the firmware via USB Serial. The firmware presents a CLI (Command Line Interface). The app sends text commands (e.g., `scanap`) and parses the text response.
*   **Hardware Support:** The firmware supports many devices (Flipper Zero Dev Board, M5Stick, custom boards). Always verify the target in `configs.h`.

## 5. Recent Updates (Dec 2025)
*   **Binary Protocol (v1.1):** High-speed binary streaming (`RESP_SCAN_DATA`) implemented for AP, Station, BLE, and GPS data.
*   **Targeted Attacks:** Android App now supports "Long Press" on APs and Stations to trigger specific Deauth attacks using the new `CMD_ATTACK` (0x04) binary command.
*   **Station Targeting:** Fixed data model to include BSSID in Station objects, enabling precise targeting of client devices.
