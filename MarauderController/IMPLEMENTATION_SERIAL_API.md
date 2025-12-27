# Serial Raw IO API & Firmware Integration Guide

## Overview
This document details the architecture, protocols, and requirements for the **Serial Raw IO API** and **Seamless Firmware Integration** implemented in the `MarauderController` app.

**Target Audience:** Firmware Developers, Contributors, and AI Assistants maintaining the ESP32 Marauder firmware.

---

## 1. Architecture

The Android application uses a layered architecture to manage USB serial communication, moving from raw byte streams to structured data and finally to high-level firmware management.

### Key Components

*   **`SerialConnectionManager` (Data Layer)**
    *   **Role:** The low-level driver wrapper.
    *   **Capabilities:**
        *   Standard Text I/O: for CLI commands (UTF-8).
        *   **Binary I/O:** for machine-to-machine (M2M) communication (Raw Bytes).
        *   **State Machine:** Parses incoming byte streams into either text lines (CLI) or `BinaryPacket` objects.
    *   **Interfaces:**
        *   `receivedData: StateFlow<String>` (CLI output)
        *   `binaryEvents: SharedFlow<BinaryPacket>` (M2M packets)
        *   `sendRawData(data: ByteArray, length: Int)`

*   **`SerialProtocol.kt` & `MarauderBinaryProtocol.kt`**
    *   **Role:** The "Contract" between App and Firmware.
    *   **Definitions:** Contains command bytes (`CMD_*`), response bytes (`RESP_*`), and packet structure rules.

*   **`FirmwareUpdateManager`**
    *   **Role:** Orchestrates the OTA update process using the Binary Protocol.
    *   **Flow:** Checks version -> Initiates Update Mode -> Streams Binary -> Verifies -> Reboots.

---

## 2. Protocols

The connection operates in two modes simultaneously: **Text Mode** (Legacy) and **Binary Mode** (New).

### 2.1 Binary Protocol Structure (`MarauderBinaryProtocol`)
To enable robust machine communication (like file transfers), a binary packet structure is used.

**Packet Format:**
```
[START_BYTE] [CMD_ID] [LENGTH] [PAYLOAD...] [END_BYTE]
```

*   **START_BYTE:** `0xA5`
*   **END_BYTE:** `0x5A`
*   **CMD_ID:** 1 Byte (defined below)
*   **LENGTH:** 1 Byte (0-255) indicating payload size.
*   **PAYLOAD:** `LENGTH` bytes of data.

**Command IDs (App -> Firmware):**
*   `0x00`: PING
*   `0x07`: UPDATE_START (Payload: optional metadata)

**Response IDs (Firmware -> App):**
*   `0x00`: ACK (Command received/processed)
*   `0x01`: NACK (Error)
*   `0x02`: PONG (Reply to Ping)

### 2.2 Firmware Update Flow
The seamless update process relies on the following handshake:

1.  **Initiation:**
    *   **App sends:** `CMD_UPDATE` (or equivalent text command `#update start`).
    *   **Firmware action:** Suspend WiFi/Bluetooth tasks, clear serial buffers, prepare flash partition.
    *   **Firmware replies:** `ACK` (or `#UPDATE:READY` text).

2.  **Streaming:**
    *   **App sends:** Raw binary chunks (up to 1KB, flow-controlled).
    *   **Firmware action:** Write bytes to OTA partition.
    *   **Note:** The app uses `sendRawData` to push valid firmware images found in `assets/firmware/`.

3.  **Completion:**
    *   **App sends:** `#update complete` (or binary equivalent).
    *   **Firmware action:** Verify checksum, set boot partition, reboot.

---

## 3. Firmware Requirements

To support this integration, the ESP32 firmware **MUST** implement the following:

### 3.1 Serial Loop Modification
The main serial loop must be updated to detect the `START_BYTE` (`0xA5`) and switch to a binary parser state machine, bypassing the standard CLI text parser for that packet.

**Pseudo-code for Firmware:**
```cpp
void loop() {
  if (Serial.available()) {
    byte b = Serial.read();
    
    // Check for Binary Protocol Start
    if (b == 0xA5) {
      handleBinaryPacket(); 
      return;
    }
    
    // Fallback to existing CLI text parser
    handleTextCommand(b);
  }
}
```

### 3.2 Update Command Handler
Implement a specific handler for the update sequence that accepts raw stream data without interpreting it as CLI commands.

*   **Buffer Size:** Ensure RX buffer is at least 1024 bytes.
*   **Timeout:** Implement a watchdog to reset state if stream hangs for > 5 seconds.

### 3.3 Hardware Compatibility
The app organizes firmware by hardware type. The firmware must correctly report its hardware revision string via the `#hardware` command or binary equivalent to ensure the app sends the correct file.

---

## 4. Asset Management (App Side)

The application sources firmware binaries from the local assets folder:

*   **Path:** `app/src/main/assets/firmware/<device_model>/esp32_marauder.bin`
*   **Supported Models:**
    *   `flipper` (Flipper Zero Dev Board)
    *   `m5stickc` (M5Stick-C Plus)
    *   `marauder_v6` (Marauder V6)
    *   ... (see `FirmwareManager.kt` for full list)

**Action Required:**
When building the APK, ensure the latest `esp32_marauder.bin` for each target is placed in the corresponding directory.

---

**Document Version:** 1.0.0
**Last Updated:** December 27, 2025
