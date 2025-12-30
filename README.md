# ESP32 Marauder (E32K Fork)
<p align="center"><img alt="Marauder logo" src="https://github.com/justcallmekoko/ESP32Marauder/blob/master/pictures/marauder_skull_patch_04_full_final.png?raw=true" width="300"></p>

## Overview
This is a modernized fork of the ESP32 Marauder, featuring a **High-Speed Binary Protocol** and a completely redesigned **Android Controller**.

### Key Features
*   **Binary Protocol (v1.1):** Replaces slow text-based parsing with a compact binary stream for real-time AP/Station/BLE data ingestion.
*   **Android Controller (Nexus):** A new Jetpack Compose application with a cyberpunk aesthetic, supporting:
    *   **Live Dashboard:** Real-time metrics and scanning.
    *   **File System Manager:** List, delete, and download (PCAP) files from the SD card.
    *   **Targeted Attacks:** Precise deauthentication using specific AP/Station MACs.
    *   **Terminal:** Integrated serial terminal for low-level control.
*   **Firmware Improvements:** Optimized scanning logic, binary packet generation, and reduced latency.

## Project Structure
*   `esp32_marauder/`: Core firmware (C++/Arduino).
*   `MarauderController/`: Android application (Kotlin/Compose).
*   `BINARY_PROTOCOL_SCHEMA.md`: Documentation of the custom binary protocol.

## Getting Started

### Firmware
1.  Open `esp32_marauder/esp32_marauder.ino` in Arduino IDE.
2.  Select your board in `configs.h` (Default: `ESP32_LDDB`).
3.  Compile and upload.

### Android App
1.  Open `MarauderController/` in Android Studio.
2.  Build and install the APK (`./gradlew assembleDebug`).
3.  Connect your ESP32 via USB OTG.

## License
MIT License. See [LICENSE](LICENSE) for details.

---
*Original Project by [justcallmekoko](https://github.com/justcallmekoko/ESP32Marauder)*
