# Roadmap to 100% Tool & Attack Support
**Project:** ESP32 Marauder (Firmware & Android Controller)
**Target:** Full Parity with CLI/On-Screen capabilities via High-Speed Binary Protocol.

---

## Phase 1: The Binary Foundation (Completed)
*   [x] **Protocol Schema:** Defined `BINARY_PROTOCOL_SCHEMA.md` (v1.1).
*   [x] **Command Infrastructure:** Implemented `CMD_GENERIC_REQ` for universal mode triggering.
*   [x] **UI Modernization:** "Nexus" Dark Theme and Dashboard implemented.
*   [x] **Proof of Concept:** AP Scan Binary Hook (`RESP_SCAN_DATA`) injected into firmware.

---

## Phase 2: High-Speed Data Ingestion (Immediate Priority)
**Goal:** Replace slow Serial Text parsing with real-time Binary parsing for all scanning modes.

### 2.1 Firmware Hooks (`esp32_marauder/`)
*   [x] **Station (Client) Scan:**
    *   *Action:* Inject `binary_obj.sendResponse(RESP_SCAN_DATA, ...)` into `WiFiScan::stationSnifferCallback`.
    *   *Payload:* `[Type: STA] [RSSI] [MAC] [BSSID]`.
*   [ ] **Bluetooth/BLE:**
    *   *Action:* Inject hooks into `WiFiScan.cpp` BLE callbacks.
    *   *Payload:* `[Type: BLE] [RSSI] [MAC] [Name_Len] [Name]`.
*   [ ] **GPS/Wardriving:**
    *   *Action:* Stream NMEA or structured GPS data via binary packets during scan loops.

### 2.2 Android Parsing (`MarauderController/`)
*   [x] **Binary Parser Implementation:**
    *   *Action:* Update `SerialConnectionManager.kt` to handle `RESP_SCAN_DATA` (0x20).
    *   *Logic:* Deserialize the payload based on the first byte (Type ID) and update `MarauderRepository` flows directly, bypassing the regex text parser.

---

## Phase 3: Offensive Operations ("The Armory")
**Goal:** granular control over specific attacks (Deauth, Beacon Spam, etc.).

### 3.1 Targeted Attacks
*   **Protocol Update:** Define specific payloads for `CMD_ATTACK`.
    *   `[AttackID] [Target_MAC] [Channel] [Timeout]`
*   **UI Implementation:**
    *   Create `AttackConfigDialog` in Android.
    *   Allow long-pressing an AP or Station in the list to "Target for Attack".

### 3.2 Complex Attacks
*   **Evil Portal:**
    *   *Requirement:* File upload capability (HTML pages) via binary stream.
    *   *UI:* "Portal Manager" tab to select active phish pages.
*   **Rick Roll / Beacon Spam:**
    *   *Requirement:* sending custom SSID lists via binary packets.

---

## Phase 4: File System & Forensics
**Goal:** Manage captured data (PCAPs, Logs) without removing the SD card.

*   **File Transfer Protocol (FTP over Serial):**
    *   Implement `CMD_FILE_LIST`, `CMD_FILE_READ`, `CMD_FILE_DELETE`.
    *   Enable downloading `.pcap` files directly to the Android "Downloads" folder.
*   **Scripting:**
    *   Editor interface for `script.txt` on Android.
    *   Upload/Execute automation scripts.

---

## Phase 5: Hardware Abstraction & Maintenance
*   **OTA Updates:**
    *   Allow flashing `esp32_marauder.bin` directly from the App using `CMD_UPDATE_START` (Already partially scaffolded).
*   **Device Config:**
    *   Binary read/write of `settings.json` for persistent configuration (Channel hopping delay, display orientation, etc.).

---

## Execution Plan (Next Steps)

1.  **Android Parser:** Implement the `RESP_SCAN_DATA` handler in Kotlin to prove the AP scan works 10x faster.
2.  **Firmware Station Hook:** Add the binary hook for Client/Station scanning.
3.  **Targeting UI:** Build the "Long Press -> Deauth" flow in the Android App.
