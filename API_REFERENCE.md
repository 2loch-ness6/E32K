# E32K API Reference

## Overview

This document provides a comprehensive reference for all available commands, responses, and data structures in the ESP32 Marauder Binary Protocol v1.1.

## Table of Contents

- [Commands (Android → ESP32)](#commands-android--esp32)
- [Responses (ESP32 → Android)](#responses-esp32--android)
- [Data Types](#data-types)
- [Android API](#android-api)
- [Error Codes](#error-codes)

---

## Commands (Android → ESP32)

### System Commands

#### CMD_PING (0x00)

**Description:** Connectivity check / keepalive  
**Payload:** None  
**Response:** RESP_PONG (0x02)

**Example:**
```
Send:    [0xA5][0x00][0x00][0x5A]
Receive: [0xA5][0x02][0x00][0x5A]
```

**Use Case:** Verify device is responsive before sending other commands

---

#### CMD_REBOOT (0x06)

**Description:** Restart the ESP32 device  
**Payload:** None  
**Response:** RESP_ACK (0x00) before reboot

**Example:**
```
Send:    [0xA5][0x06][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]
         (Device reboots)
```

**Warning:** Device will disconnect. Reconnection required after ~5 seconds.

---

#### CMD_UPDATE_START (0x07)

**Description:** Enter OTA firmware update mode  
**Payload:** None  
**Response:** RESP_ACK on success, RESP_NACK if cannot enter update mode

**Example:**
```
Send:    [0xA5][0x07][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]  (Success)
```

**Note:** After ACK, send firmware binary data. Implementation details in OTA guide.

---

### WiFi Scan Commands

#### CMD_SCAN_AP (0x01)

**Description:** Start Access Point scanning  
**Payload:** None  
**Response:** RESP_ACK, followed by multiple RESP_SCAN_DATA packets (Type 0x01)

**Example:**
```
Send:    [0xA5][0x01][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]  (ACK)
         [0xA5][0x20][...][0x5A]   (AP data)
         [0xA5][0x20][...][0x5A]   (AP data)
         ...
```

**Scan Data Format (Type 0x01):**
```
[Type:1][RSSI:1][Channel:1][MAC:6][Auth:1][SSID_Len:1][SSID:Var]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | Always 0x01 |
| RSSI | 1 | int8 | Signal strength (-128 to 0) |
| Channel | 1 | uint8 | WiFi channel (1-14, 36+) |
| MAC | 6 | uint8[] | BSSID (MAC address) |
| Auth | 1 | uint8 | Auth mode (see wifi_auth_mode_t) |
| SSID_Len | 1 | uint8 | Length of SSID (0-32) |
| SSID | Var | char[] | Network name |

**Auth Mode Values:**
- 0: OPEN
- 1: WEP
- 2: WPA_PSK
- 3: WPA2_PSK
- 4: WPA_WPA2_PSK
- 5: WPA2_ENTERPRISE
- 6: WPA3_PSK
- 7: WPA2_WPA3_PSK

---

#### CMD_SCAN_STA (0x02)

**Description:** Start Station (client device) scanning  
**Payload:** None  
**Response:** RESP_ACK, followed by RESP_SCAN_DATA packets (Type 0x02)

**Example:**
```
Send:    [0xA5][0x02][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]  (ACK)
         [0xA5][0x20][...][0x5A]   (Station data)
```

**Scan Data Format (Type 0x02):**
```
[Type:1][RSSI:1][MAC:6][BSSID:6][Channel:1]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | Always 0x02 |
| RSSI | 1 | int8 | Signal strength |
| MAC | 6 | uint8[] | Client device MAC |
| BSSID | 6 | uint8[] | Connected AP's MAC |
| Channel | 1 | uint8 | Operating channel |

---

#### CMD_STOP_SCAN (0x03)

**Description:** Stop current scan or attack  
**Payload:** None  
**Response:** RESP_ACK

**Example:**
```
Send:    [0xA5][0x03][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]
```

**Note:** Works for any active scanning or attack mode.

---

### Attack Commands

#### CMD_ATTACK (0x04)

**Description:** Execute a specific attack  
**Payload:** Varies by attack type  
**Response:** RESP_ACK on success, RESP_NACK if invalid

**Attack Type 0x01: Targeted Deauth**

**Payload:**
```
[Type:1][Channel:1][AP_MAC:6][Station_MAC:6]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | 0x01 for deauth |
| Channel | 1 | uint8 | Target channel |
| AP_MAC | 6 | uint8[] | Access Point BSSID |
| Station_MAC | 6 | uint8[] | Client MAC (or FF:FF:FF:FF:FF:FF for broadcast) |

**Example:**
```
Send: [0xA5][0x04][0x0E]
      [0x01]                         (Deauth type)
      [0x06]                         (Channel 6)
      [0xAA][0xBB][0xCC][0xDD][0xEE][0xFF]  (AP MAC)
      [0xFF][0xFF][0xFF][0xFF][0xFF][0xFF]  (Broadcast to all clients)
      [0x5A]
```

**Attack Type 0x02: Beacon Spam** (Planned)

**Payload:**
```
[Type:1][Count:2][SSID_Len:1][SSID:Var]
```

**Attack Type 0x03: Rick Roll** (Planned)

**Payload:**
```
[Type:1][Channel:1]
```

---

#### CMD_GENERIC_REQ (0x10)

**Description:** Execute any WiFi/BT mode by ID (legacy compatibility)  
**Payload:** [ModeID:1]  
**Response:** RESP_ACK

**Example:**
```
Send: [0xA5][0x10][0x01][0x20][0x5A]
      Mode 0x20 = WIFI_SCAN_WAR_DRIVE
```

**Common Mode IDs:**
- 0x02: WIFI_SCAN_AP
- 0x1A: WIFI_SCAN_STATION (26)
- 0x20: WIFI_SCAN_WAR_DRIVE (32)
- 0x08: WIFI_ATTACK_BEACON_SPAM
- 0x09: WIFI_ATTACK_RICK_ROLL

---

### File System Commands

#### CMD_FS_LIST (0x08)

**Description:** List files on SD card  
**Payload:** None  
**Response:** Sequence of RESP_SCAN_DATA (Type 0x05) + final RESP_ACK

**Example:**
```
Send:    [0xA5][0x08][0x00][0x5A]
Receive: [0xA5][0x00][0x00][0x5A]          (ACK - starting)
         [0xA5][0x20][...][0x5A]           (File 1)
         [0xA5][0x20][...][0x5A]           (File 2)
         ...
         [0xA5][0x00][0x00][0x5A]          (ACK - done)
```

**File Entry Format (Type 0x05):**
```
[Type:1][Size:4][Name_Len:1][Name:Var]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | Always 0x05 |
| Size | 4 | uint32 | File size in bytes (Little-Endian) |
| Name_Len | 1 | uint8 | Filename length |
| Name | Var | char[] | Filename (e.g., "capture.pcap") |

---

#### CMD_FS_DELETE (0x09)

**Description:** Delete a file from SD card  
**Payload:** [Name_Len:1][Name:Var]  
**Response:** RESP_ACK on success, RESP_NACK if file not found or error

**Example:**
```
Send: [0xA5][0x09][0x0C]
      [0x0B]                                (Name length = 11)
      ['c']['a']['p']['t']['u']['r']['e']['.']['p']['c']['a']['p']
      [0x5A]
```

---

#### CMD_FS_READ (0x0A)

**Description:** Download a file from SD card  
**Payload:** [Name_Len:1][Name:Var]  
**Response:** Sequence of RESP_SCAN_DATA (Type 0x06) chunks + final RESP_ACK

**Example:**
```
Send:    [0xA5][0x0A][0x0C][0x0B]['capture.pcap'][0x5A]
Receive: [0xA5][0x20][...][0x5A]  (Chunk 0)
         [0xA5][0x20][...][0x5A]  (Chunk 1)
         ...
         [0xA5][0x20][0x04][0x06][SeqHi][SeqLo][0x00][0x5A]  (EOF marker)
         [0xA5][0x00][0x00][0x5A]  (ACK - complete)
```

**File Data Chunk Format (Type 0x06):**
```
[Type:1][Seq:2][Data_Len:1][Data:Var]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | Always 0x06 |
| Seq | 2 | uint16 | Chunk sequence number (Little-Endian) |
| Data_Len | 1 | uint8 | Bytes in this chunk (0 = EOF) |
| Data | Var | uint8[] | File data (up to 190 bytes) |

**EOF Detection:** When Data_Len = 0, download is complete.

---

## Responses (ESP32 → Android)

### RESP_ACK (0x00)

**Description:** Command accepted and executed  
**Payload:** None

**Example:**
```
[0xA5][0x00][0x00][0x5A]
```

---

### RESP_NACK (0x01)

**Description:** Command rejected or failed  
**Payload:** None

**Reasons:**
- Invalid command
- Invalid payload length
- Resource unavailable (e.g., SD card not present)
- Operation failed

**Example:**
```
[0xA5][0x01][0x00][0x5A]
```

---

### RESP_PONG (0x02)

**Description:** Response to CMD_PING  
**Payload:** None

**Example:**
```
[0xA5][0x02][0x00][0x5A]
```

---

### RESP_SCAN_DATA (0x20)

**Description:** Generic data container for various data types  
**Payload:** [Type:1][Data:Var]

**Supported Types:**
- 0x01: Access Point
- 0x02: Station
- 0x03: BLE Device
- 0x04: GPS Data
- 0x05: File Entry
- 0x06: File Data Chunk

**(See individual command sections for payload formats)**

---

## Data Types

### Bluetooth Device (Type 0x03)

```
[Type:1][RSSI:1][MAC:6][Name_Len:1][Name:Var]
```

| Field | Size | Description |
|-------|------|-------------|
| Type | 1 | Always 0x03 |
| RSSI | 1 | Signal strength |
| MAC | 6 | Bluetooth MAC address |
| Name_Len | 1 | Device name length |
| Name | Var | Device name string |

---

### GPS Data (Type 0x04)

```
[Type:1][Lat:8][Lon:8][Alt:8][Sats:1][Fix:1]
```

| Field | Size | Type | Description |
|-------|------|------|-------------|
| Type | 1 | uint8 | Always 0x04 |
| Lat | 8 | double | Latitude (Little-Endian) |
| Lon | 8 | double | Longitude (Little-Endian) |
| Alt | 8 | double | Altitude in meters |
| Sats | 1 | uint8 | Number of satellites |
| Fix | 1 | uint8 | GPS fix (0=no fix, 1=fix) |

---

## Android API

### Repository Methods

```kotlin
class MarauderRepository(context: Context) {
    
    // Connection
    fun connect(usbDevice: UsbDevice)
    fun disconnect()
    val connectionState: StateFlow<ConnectionState>
    
    // WiFi Scanning
    fun startAPScan()
    fun startStationScan()
    fun stopScan()
    val accessPoints: StateFlow<List<AccessPoint>>
    val stations: StateFlow<List<Station>>
    
    // Attacks
    fun executeDeauth(channel: Int, apMac: String, staMac: String)
    fun startBeaconSpam()
    fun startRickRoll()
    val currentAttack: StateFlow<AttackType?>
    
    // File System
    fun refreshFileList()
    fun deleteFile(filename: String)
    fun downloadFile(filename: String): Flow<ByteArray>
    val fileList: StateFlow<List<FileEntry>>
    val downloadProgress: StateFlow<DownloadProgress?>
    
    // GPS
    val gpsData: StateFlow<GpsData?>
    
    // System
    fun reboot()
    fun getDeviceInfo()
    val deviceInfo: StateFlow<DeviceInfo?>
}
```

---

### ViewModel Methods

```kotlin
class MarauderViewModel(
    private val repository: MarauderRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // Exposed StateFlows
    val connectionState: StateFlow<ConnectionState>
    val accessPoints: StateFlow<List<AccessPoint>>
    val stations: StateFlow<List<Station>>
    val fileList: StateFlow<List<FileEntry>>
    val downloadProgress: StateFlow<DownloadProgress?>
    
    // Actions
    fun connect(device: UsbDevice)
    fun disconnect()
    fun startAPScan()
    fun stopScan()
    fun executeDeauth(channel: Int, apMac: String, staMac: String)
    fun refreshFileList()
    fun downloadFile(context: Context, filename: String)
    fun deleteFile(filename: String)
    
    // Preferences
    fun setThemeMode(mode: ThemeMode)
    fun setAutoConnect(enabled: Boolean)
}
```

---

## Error Codes

### Protocol Errors

| Code | Name | Description | Recovery |
|------|------|-------------|----------|
| NACK | Command Rejected | Invalid command or params | Retry with correct params |
| Timeout | No Response | Device not responding | Check connection, reboot device |
| Parse Error | Malformed Packet | Unexpected data format | Ignore packet, continue |
| Buffer Overflow | Packet Too Large | Payload > 255 bytes | Reduce payload size |

---

### Android Exceptions

```kotlin
sealed class MarauderException : Exception() {
    class DeviceNotConnected : MarauderException()
    class CommandTimeout : MarauderException()
    class InvalidResponse : MarauderException()
    class FileNotFound : MarauderException()
    class SDCardNotPresent : MarauderException()
}
```

---

## Usage Examples

### Example 1: Scan and Attack

```kotlin
// In ViewModel
viewModelScope.launch {
    // Start scan
    repository.startAPScan()
    
    // Wait for APs to populate
    delay(5000)
    
    // Get target
    val targetAP = repository.accessPoints.value.firstOrNull { 
        it.ssid == "TargetNetwork" 
    }
    
    if (targetAP != null) {
        // Execute attack
        repository.executeDeauth(
            channel = targetAP.channel,
            apMac = targetAP.bssid,
            staMac = "FF:FF:FF:FF:FF:FF" // Broadcast
        )
        
        // Run for 30 seconds
        delay(30000)
        
        // Stop
        repository.stopScan()
    }
}
```

---

### Example 2: Download File with Progress

```kotlin
// In ViewModel
fun downloadFile(context: Context, filename: String) {
    viewModelScope.launch {
        val file = File(context.getExternalFilesDir(null), filename)
        val outputStream = FileOutputStream(file)
        
        try {
            repository.downloadFile(filename).collect { chunk ->
                outputStream.write(chunk)
                
                // Progress is automatically updated in repository
            }
            
            // Success
            showToast("Downloaded to ${file.absolutePath}")
        } catch (e: Exception) {
            showToast("Download failed: ${e.message}")
        } finally {
            outputStream.close()
        }
    }
}

// In UI
@Composable
fun FileScreen(viewModel: MarauderViewModel) {
    val progress by viewModel.downloadProgress.collectAsState()
    
    progress?.let {
        LinearProgressIndicator(progress = it.progress)
        Text("${it.progressPercentage}% - ${formatSize(it.bytesDownloaded)}")
    }
}
```

---

## Protocol Compliance

### Required Behaviors

**Firmware:**
- ✅ Must send START_BYTE before every packet
- ✅ Must send END_BYTE after every packet
- ✅ Must send ACK/NACK for all commands
- ✅ Must validate payload length before parsing
- ✅ Must use Little-Endian for multi-byte integers
- ✅ Must not block Serial for extended periods

**Android:**
- ✅ Must validate START_BYTE before parsing
- ✅ Must validate END_BYTE after parsing
- ✅ Must handle NACK gracefully
- ✅ Must timeout on missing responses (5-10 seconds)
- ✅ Must parse data in correct byte order
- ✅ Must handle incomplete packets

---

## Versioning

**Current Version:** Binary Protocol v1.1  
**Firmware Version:** v1.9.0  
**Android App Version:** 1.1.0

**Compatibility Matrix:**

| Firmware | Android | Compatible |
|----------|---------|------------|
| v1.9.0 | 1.1.0 | ✅ Full |
| v1.8.x | 1.1.0 | ⚠️ Limited (legacy text mode) |
| v1.9.0 | 1.0.x | ❌ No binary protocol |

---

## References

- [BINARY_PROTOCOL_SCHEMA.md](./BINARY_PROTOCOL_SCHEMA.md) - Formal protocol spec
- [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) - Implementation patterns
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Testing procedures

---

**Document Version:** 1.0  
**Last Updated:** 2026-01-05  
**Maintainer:** E32K Project Team
