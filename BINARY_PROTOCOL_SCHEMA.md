# Marauder Binary Protocol Schema (v1.1)

This document defines the binary communication protocol between the ESP32 Marauder firmware and the Android Controller.

## Packet Structure
All multi-byte fields are **Little Endian** unless specified otherwise.

| Field | Size | Description |
| :--- | :--- | :--- |
| **START** | 1 Byte | `0xA5` (Start of Frame) |
| **CMD** | 1 Byte | Command Identifier (0x00 - 0xFF) |
| **LEN** | 1 Byte | Length of Payload (0 - 255) |
| **PAYLOAD** | N Bytes | Data (if LEN > 0) |
| **END** | 1 Byte | `0x5A` (End of Frame) |

---

## Command Definitions

### System Commands

#### `CMD_PING` (0x00)
*   **Description:** Connectivity check.
*   **Payload:** None.
*   **Response:** `RESP_PONG` (0x02).

#### `CMD_REBOOT` (0x06)
*   **Description:** Restarts the ESP32.
*   **Payload:** None.
*   **Response:** `RESP_ACK` (0x00) before reboot.

#### `CMD_UPDATE_START` (0x07)
*   **Description:** Enter OTA Update Mode.
*   **Payload:** None.
*   **Response:** `RESP_ACK` (0x00) on success, `RESP_NACK` (0x01) on fail.

---

### WiFi Commands

#### `CMD_SCAN_AP` (0x01)
*   **Description:** Start Access Point Scan.
*   **Internal Map:** `StartScan(WIFI_SCAN_AP)`
*   **Payload:** None.
*   **Response:** `RESP_ACK`.

#### `CMD_SCAN_STA` (0x02)
*   **Description:** Start Station (Client) Scan.
*   **Internal Map:** `StartScan(WIFI_SCAN_STATION)`
*   **Payload:** None.
*   **Response:** `RESP_ACK`.

#### `CMD_STOP_SCAN` (0x03)
*   **Description:** Stop current scan or attack.
*   **Internal Map:** `StopScan(currentScanMode)`
*   **Payload:** None.
*   **Response:** `RESP_ACK`.

#### `CMD_ATTACK` (0x04)
*   **Description:** Start a specific attack.
*   **Payload:** `[Type (1)] [Channel (1)] [AP_MAC (6)] [Station_MAC (6)]` (Total 14 Bytes)
    *   **Type 0x01:** Deauth Attack (`WIFI_ATTACK_DEAUTH_MANUAL`)
        *   `Channel`: Target Channel (1-14).
        *   `AP_MAC`: Target Access Point BSSID.
        *   `Station_MAC`: Target Station MAC (or `FF:FF:FF:FF:FF:FF` for broadcast).
    *   **Type 0x02:** Beacon Spam (Implementation Pending)
    *   **Type 0x03:** Rick Roll (Implementation Pending)
*   **Response:** `RESP_ACK`.

---

### Extended Commands

#### `CMD_GENERIC_REQ` (0x10)
*   **Description:** Execute any `WIFI_` or `BT_` mode by ID.
*   **Payload:** `[ModeID (1 Byte)]`
    *   `ModeID`: Matches `WiFiScan.h` constants (e.g., `WIFI_SCAN_WAR_DRIVE = 32`).
*   **Response:** `RESP_ACK`.

### File System Commands

#### `CMD_FS_LIST` (0x08)
*   **Description:** Request a list of files in the root directory.
*   **Payload:** None.
*   **Response:** Sequence of `RESP_SCAN_DATA` (Type 0x05: File Entry) followed by `RESP_ACK`.

#### `CMD_FS_DELETE` (0x09)
*   **Description:** Delete a specific file.
*   **Payload:** `[Filename Length (1)] [Filename (Variable)]`
*   **Response:** `RESP_ACK` or `RESP_NACK`.

#### `CMD_FS_READ` (0x0A)
*   **Description:** Request file content (Download).
*   **Payload:** `[Filename Length (1)] [Filename (Variable)]`
*   **Response:** Sequence of `RESP_SCAN_DATA` (Type 0x06: File Data Chunk) followed by `RESP_ACK`.

---

## Response & Data Codes
*   `RESP_ACK` (0x00): Command accepted.
*   `RESP_NACK` (0x01): Command failed or invalid.
*   `RESP_PONG` (0x02): Pong response.
*   `RESP_SCAN_DATA` (0x20): Generic Data Container.

### `RESP_SCAN_DATA` Payload Structure
| Offset | Type | Description |
| :--- | :--- | :--- |
| 0 | Byte | **Data Type** (0x01=AP, 0x02=Station, 0x03=BLE, 0x04=GPS, 0x05=FileEntry, 0x06=FileData) |
| 1 | N Bytes | **Data Object** (See definitions below) |

#### Data Object: Access Point (Type 0x01)
*   `RSSI` (1 Byte, signed)
*   `Channel` (1 Byte)
*   `MacAddr` (6 Bytes)
*   `Auth` (1 Byte, see `wifi_auth_mode_t`)
*   `SSID_Len` (1 Byte)
*   `SSID` (Variable)

#### Data Object: Station (Type 0x02)
*   `RSSI` (1 Byte, signed)
*   `MacAddr` (6 Bytes)
*   `BssidAddr` (6 Bytes)
*   `Channel` (1 Byte)

#### Data Object: BLE Device (Type 0x03)
*   `RSSI` (1 Byte, signed)
*   `MacAddr` (6 Bytes)
*   `Name_Len` (1 Byte)
*   `Name` (Variable)

#### Data Object: GPS (Type 0x04)
*   `Lat` (8 Bytes, Double)
*   `Lon` (8 Bytes, Double)
*   `Alt` (8 Bytes, Double)
*   `Sats` (1 Byte)
*   `Fix` (1 Byte, 0 or 1)

#### Data Object: File Entry (Type 0x05)
*   `Size` (4 Bytes, uint32)
*   `Name_Len` (1 Byte)
*   `Name` (Variable)

#### Data Object: File Data Chunk (Type 0x06)
*   `Sequence` (2 Bytes, uint16)
*   `Data_Len` (1 Byte)
*   `Data` (Variable)
*   **Note:** A chunk with `Data_Len = 0` indicates End of File (EOF).

**(To be expanded as implementation matures)**