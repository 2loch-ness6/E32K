# E32K Core Integration Guide

## Overview

This guide documents the integration patterns between the ESP32 Marauder firmware and the Android Controller application using the High-Speed Binary Protocol v1.1.

## Architecture

### Three-Layer Architecture

```
┌─────────────────────────────────────┐
│     Android Application (Kotlin)    │
│  ┌────────────────────────────────┐ │
│  │  UI Layer (Jetpack Compose)    │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  ViewModel (State Management)  │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  Repository (Data Layer)       │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  Serial Manager (USB OTG)      │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
              ↕ Binary Protocol
┌─────────────────────────────────────┐
│        ESP32 Firmware (C++)         │
│  ┌────────────────────────────────┐ │
│  │  BinaryInterface.cpp           │ │
│  │  (Protocol Handler)            │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  WiFiScan.cpp                  │ │
│  │  (Scanning & Attacks)          │ │
│  └────────────────────────────────┘ │
│  ┌────────────────────────────────┐ │
│  │  SDInterface.cpp               │ │
│  │  (File System)                 │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Binary Protocol v1.1

### Packet Structure

All packets follow this format:

```
[START:1] [CMD:1] [LEN:1] [PAYLOAD:N] [END:1]
```

- **START**: `0xA5` (Start of Frame marker)
- **CMD**: Command/Response identifier (0x00 - 0xFF)
- **LEN**: Payload length (0 - 255 bytes)
- **PAYLOAD**: Variable-length data
- **END**: `0x5A` (End of Frame marker)

### Command Flow Examples

#### 1. WiFi Access Point Scanning

**Android → ESP32:**
```
[0xA5] [0x01] [0x00] [0x5A]
```
- CMD: `0x01` (CMD_SCAN_AP)
- LEN: `0x00` (No payload)

**ESP32 → Android (ACK):**
```
[0xA5] [0x00] [0x00] [0x5A]
```
- CMD: `0x00` (RESP_ACK)

**ESP32 → Android (Scan Data - Multiple packets):**
```
[0xA5] [0x20] [LEN] [0x01][RSSI][Ch][MAC:6][Auth][SSID_Len][SSID...] [0x5A]
```
- CMD: `0x20` (RESP_SCAN_DATA)
- Payload Type: `0x01` (Access Point)

#### 2. Station Scanning

**Android → ESP32:**
```
[0xA5] [0x02] [0x00] [0x5A]
```
- CMD: `0x02` (CMD_SCAN_STA)

**ESP32 → Android (Station Data):**
```
[0xA5] [0x20] [0x0F] [0x02][RSSI][MAC:6][BSSID:6][Ch] [0x5A]
```
- CMD: `0x20` (RESP_SCAN_DATA)
- Payload Type: `0x02` (Station)
- LEN: `0x0F` (15 bytes)

#### 3. Targeted Deauth Attack

**Android → ESP32:**
```
[0xA5] [0x04] [0x0E] [0x01][Ch][AP_MAC:6][STA_MAC:6] [0x5A]
```
- CMD: `0x04` (CMD_ATTACK)
- LEN: `0x0E` (14 bytes)
- Attack Type: `0x01` (Deauth Manual)
- Ch: Target channel
- AP_MAC: Access Point BSSID
- STA_MAC: Station MAC (or FF:FF:FF:FF:FF:FF for broadcast)

#### 4. File System List

**Android → ESP32:**
```
[0xA5] [0x08] [0x00] [0x5A]
```
- CMD: `0x08` (CMD_FS_LIST)

**ESP32 → Android (File Entries - Multiple):**
```
[0xA5] [0x20] [LEN] [0x05][Size:4][NameLen][Name...] [0x5A]
```
- CMD: `0x20` (RESP_SCAN_DATA)
- Payload Type: `0x05` (File Entry)

**ESP32 → Android (End):**
```
[0xA5] [0x00] [0x00] [0x5A]
```
- CMD: `0x00` (RESP_ACK - List complete)

#### 5. File Download

**Android → ESP32:**
```
[0xA5] [0x0A] [LEN] [NameLen][Name...] [0x5A]
```
- CMD: `0x0A` (CMD_FS_READ)
- Payload: Filename length + filename

**ESP32 → Android (Data Chunks):**
```
[0xA5] [0x20] [LEN] [0x06][Seq:2][DataLen][Data...] [0x5A]
```
- CMD: `0x20` (RESP_SCAN_DATA)
- Payload Type: `0x06` (File Data Chunk)
- Seq: Chunk sequence number (increments)
- DataLen: Bytes in this chunk (0 = EOF)

## Implementation Patterns

### Firmware Side (ESP32)

#### 1. Adding a New Binary Command

**Step 1:** Define command in `BinaryInterface.h`
```cpp
#define CMD_MY_NEW_COMMAND 0x0B
```

**Step 2:** Add handler in `BinaryInterface.cpp`
```cpp
void BinaryInterface::handlePacket(uint8_t cmd, uint8_t* payload, uint8_t len) {
    switch (cmd) {
        // ... existing cases ...
        
        case CMD_MY_NEW_COMMAND:
            if (len >= expectedSize) {
                // Parse payload
                uint8_t param1 = payload[0];
                uint16_t param2 = (payload[2] << 8) | payload[1]; // Little-endian
                
                // Execute logic
                bool success = doSomething(param1, param2);
                
                // Send response
                if (success) {
                    sendResponse(RESP_ACK, NULL, 0);
                } else {
                    sendResponse(RESP_NACK, NULL, 0);
                }
            } else {
                sendResponse(RESP_NACK, NULL, 0);
            }
            break;
    }
}
```

#### 2. Streaming Real-Time Data

**In WiFiScan.cpp or similar:**
```cpp
// Include the binary interface
extern BinaryInterface binary_obj;

void WiFiScan::myCallback(/* parameters */) {
    // Build binary payload
    uint8_t payload[64];
    int idx = 0;
    
    // Type identifier
    payload[idx++] = 0x07; // My data type
    
    // Add data fields
    payload[idx++] = (uint8_t)(rssi & 0xFF);
    memcpy(&payload[idx], macAddr, 6);
    idx += 6;
    
    // Send via binary protocol
    binary_obj.sendResponse(RESP_SCAN_DATA, payload, idx);
}
```

### Android Side

#### 1. Adding Command Support

**Step 1:** Define in `MarauderBinaryProtocol.kt`
```kotlin
object MarauderBinaryProtocol {
    // ... existing constants ...
    const val CMD_MY_NEW_COMMAND: Byte = 0x0B
}
```

**Step 2:** Add Repository method
```kotlin
class MarauderRepository(context: Context) {
    // ...
    
    fun executeMyCommand(param1: Int, param2: Int) {
        val payload = ByteArray(3)
        payload[0] = param1.toByte()
        payload[1] = (param2 and 0xFF).toByte()  // Little-endian
        payload[2] = ((param2 shr 8) and 0xFF).toByte()
        
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_MY_NEW_COMMAND, 
            payload.size, 
            payload
        )
        serialManager.sendBinaryCommand(packet)
    }
}
```

**Step 3:** Expose in ViewModel
```kotlin
class MarauderViewModel(...) : ViewModel() {
    fun triggerMyCommand(param1: Int, param2: Int) {
        viewModelScope.launch {
            repository.executeMyCommand(param1, param2)
        }
    }
}
```

**Step 4:** Call from UI
```kotlin
@Composable
fun MyScreen(viewModel: MarauderViewModel) {
    Button(onClick = { viewModel.triggerMyCommand(5, 1024) }) {
        Text("Execute Command")
    }
}
```

#### 2. Parsing New Data Types

**In `MarauderRepository.kt`:**
```kotlin
private fun parseScanData(payload: ByteArray) {
    if (payload.isEmpty()) return
    
    val type = payload[0].toInt()
    
    when (type) {
        // ... existing types (0x01-0x06) ...
        
        0x07 -> { // My new data type
            if (payload.size >= 8) {
                val rssi = payload[1].toByte().toInt()
                val mac = payload.copyOfRange(2, 8)
                    .joinToString(":") { "%02X".format(it) }
                
                // Update state
                val newItem = MyDataType(mac = mac, rssi = rssi)
                updateMyDataList(newItem)
            }
        }
    }
}

private fun updateMyDataList(item: MyDataType) {
    val current = _myDataList.value.toMutableList()
    val existing = current.indexOfFirst { it.mac == item.mac }
    
    if (existing >= 0) {
        current[existing] = item
    } else {
        current.add(item)
    }
    
    _myDataList.value = current
}
```

## Error Handling Best Practices

### Firmware (ESP32)

1. **Always validate payload length:**
```cpp
if (len < EXPECTED_MIN_SIZE) {
    sendResponse(RESP_NACK, NULL, 0);
    return;
}
```

2. **Check resource availability:**
```cpp
if (!sd_obj.supported) {
    sendResponse(RESP_NACK, NULL, 0);
    return;
}
```

3. **Use try-catch for parsing:**
```cpp
// Not applicable in C++, but validate inputs
```

### Android

1. **Wrap parsing in try-catch:**
```kotlin
try {
    val value = buffer.getInt()
    // Process value
} catch (e: Exception) {
    // Log error, don't crash
    Log.e("Parser", "Failed to parse: ${e.message}")
}
```

2. **Handle timeout scenarios:**
```kotlin
try {
    withTimeoutOrNull(5000) { // 5 second timeout
        // Wait for response
    } ?: run {
        // Timeout occurred
        showError("Operation timed out")
    }
} catch (e: Exception) {
    showError(e.message ?: "Unknown error")
}
```

3. **Validate data before using:**
```kotlin
if (payload.size >= expectedSize) {
    // Safe to parse
} else {
    return // Ignore malformed packet
}
```

## Performance Optimization

### Reducing Latency

1. **Batch Binary Packets (Firmware):**
   - Send multiple scan results in rapid succession
   - Don't delay between packets unless necessary

2. **Use Coroutines (Android):**
   ```kotlin
   viewModelScope.launch(Dispatchers.IO) {
       // Heavy processing off main thread
   }
   ```

3. **Debounce UI Updates:**
   ```kotlin
   val debouncedList = myDataFlow
       .debounce(100) // Only update UI every 100ms
       .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
   ```

### Memory Management

1. **Limit List Sizes:**
   ```kotlin
   val maxItems = 1000
   if (current.size >= maxItems) {
       current.removeAt(0) // Remove oldest
   }
   ```

2. **Clear Data When Appropriate:**
   ```kotlin
   fun clearOldData() {
       _accessPoints.value = emptyList()
       _stations.value = emptyList()
   }
   ```

## Testing Integration

### Firmware Testing

1. **Test with Serial Monitor:**
   - Send binary commands using serial tools
   - Use Python script to generate packets

2. **Validate Responses:**
   - Check START and END bytes
   - Verify payload length matches

### Android Testing

1. **Unit Test Protocol Parser:**
   ```kotlin
   @Test
   fun testAccessPointParsing() {
       val payload = byteArrayOf(
           0x01, // Type
           -50,  // RSSI
           1,    // Channel
           0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF, // MAC
           3,    // Auth
           4,    // SSID Length
           'T'.code.toByte(), 'e'.code.toByte(), 
           's'.code.toByte(), 't'.code.toByte()
       )
       
       // Parse and assert
       // ...
   }
   ```

2. **Integration Test with Mock Serial:**
   - Mock SerialConnectionManager
   - Inject test packets
   - Verify state updates

## Common Integration Issues

### Issue 1: Packets Not Received

**Symptoms:** Commands sent but no response

**Solutions:**
- Check START_BYTE and END_BYTE match (0xA5, 0x5A)
- Verify baud rate (typically 115200)
- Ensure binary mode is active on firmware
- Check USB cable quality

### Issue 2: Garbled Data

**Symptoms:** Parsing errors, incorrect values

**Solutions:**
- Verify Little-Endian byte order
- Check array bounds (payload.size)
- Ensure type casts are correct (signed vs unsigned)

### Issue 3: State Not Updating

**Symptoms:** UI doesn't reflect new data

**Solutions:**
- Ensure StateFlow is being updated (`.value =`)
- Verify collectAsState() in UI
- Check coroutine scope is active

### Issue 4: Memory Leaks

**Symptoms:** App slows over time

**Solutions:**
- Clear old data periodically
- Limit list sizes
- Cancel coroutines in onCleared()

## Future Integration Points

### Phase 4 Planned Features

1. **Enhanced File Transfer:**
   - Resume capability
   - Compression
   - Checksums

2. **Configuration Management:**
   - Read/write device settings
   - Persistent configuration

3. **OTA Updates:**
   - Flash firmware from Android
   - Verification and rollback

4. **Advanced Attacks:**
   - Custom beacon payloads
   - Evil Portal configuration upload
   - Script execution

## References

- [BINARY_PROTOCOL_SCHEMA.md](./BINARY_PROTOCOL_SCHEMA.md) - Complete protocol specification
- [PROJECT_STATUS.md](./PROJECT_STATUS.md) - Current implementation status
- [ROADMAP_TO_FULL_SUPPORT.md](./ROADMAP_TO_FULL_SUPPORT.md) - Future development plans

## Support

For integration issues:
1. Check the protocol schema matches firmware version
2. Review logcat output for Android errors
3. Monitor ESP32 serial output for firmware errors
4. Consult existing implementations for patterns

---

**Last Updated:** 2026-01-05  
**Protocol Version:** v1.1  
**Firmware Version:** v1.9.0  
**Android App Version:** 1.1.0
