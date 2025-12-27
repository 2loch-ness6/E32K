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
*   **Payload:** `[Type (1 Byte)] [Config (Optional)]`
    *   **Type 0x01:** Deauth Attack (`WIFI_ATTACK_DEAUTH`)
    *   **Type 0x02:** Beacon Spam (`WIFI_ATTACK_BEACON_SPAM`)
    *   **Type 0x03:** Rick Roll (`WIFI_ATTACK_RICK_ROLL`)
*   **Response:** `RESP_ACK`.

---

### Extended Commands

#### `CMD_GENERIC_REQ` (0x10)
*   **Description:** Execute any `WIFI_` or `BT_` mode by ID.
*   **Payload:** `[ModeID (1 Byte)]`
    *   `ModeID`: Matches `WiFiScan.h` constants (e.g., `WIFI_SCAN_WAR_DRIVE = 32`).
*   **Response:** `RESP_ACK`.

---

## Response & Data Codes
*   `RESP_ACK` (0x00): Command accepted.
*   `RESP_NACK` (0x01): Command failed or invalid.
*   `RESP_PONG` (0x02): Pong response.
*   `RESP_SCAN_DATA` (0x20): Generic Data Container.

### `RESP_SCAN_DATA` Payload Structure
| Offset | Type | Description |
| :--- | :--- | :--- |
| 0 | Byte | **Data Type** (0x01=AP, 0x02=Station) |
| 1 | N Bytes | **Data Object** (See definitions below) |

#### Data Object: Access Point (Type 0x01)
*   **Size:** Fixed or Variable (Currently treating as fixed for MVP)
*   Structure:
    *   `Index` (1 Byte)
    *   `RSSI` (1 Byte, signed)
    *   `Channel` (1 Byte)
    *   `MacAddr` (6 Bytes)
    *   `SSID` (Variable, null-terminated or length-prefixed) - *Implementation Note: For MVP, maybe send raw string or simplify.*

**(To be expanded as implementation matures)**