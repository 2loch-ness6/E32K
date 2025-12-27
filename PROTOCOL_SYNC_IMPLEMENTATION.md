# Protocol Sync Implementation - Firmware and Android App

## Overview
This document describes the protocol synchronization between ESP32 Marauder firmware and the Android Controller app, enabling seamless device communication for version detection, hardware identification, and system monitoring.

## Problem Statement
The original implementation had:
- Protocol commands defined in firmware (`version`, `hardware`, `heap`)
- Protocol response definitions in Android app (SerialProtocol.kt)
- **Missing**: Parser implementation to handle protocol responses
- **Missing**: Repository integration to expose device information

## Solution
Implemented complete protocol sync by:
1. Adding protocol response parsing in MarauderProtocolParser
2. Creating response types in MarauderModels
3. Integrating protocol handlers in MarauderRepository
4. Adding comprehensive unit tests

## Protocol Specification

### Firmware Commands → Android App

#### 1. Version Command
**Command:** `version`  
**Response:** `#VERSION:v1.9.0`  
**Purpose:** Query firmware version number

**Firmware Implementation (esp32_marauder/CommandLine.cpp:288-290):**
```cpp
if (input.startsWith("version")) {
  Serial.println("#VERSION:" + (String)MARAUDER_VERSION);
  return;
}
```

**Android Parser Implementation:**
```kotlin
if (trimmed.startsWith(ProtocolResponses.VERSION_PREFIX)) {
    val version = trimmed.substringAfter(ProtocolResponses.VERSION_PREFIX).trim()
    return MarauderResponse.VersionInfo(version)
}
```

#### 2. Hardware Command
**Command:** `hardware`  
**Response:** `#HARDWARE:Flipper Zero Dev Board`  
**Purpose:** Query hardware platform type

**Firmware Implementation (esp32_marauder/CommandLine.cpp:293-299):**
```cpp
if (input.startsWith("hardware")) {
  #ifdef HARDWARE_NAME
    Serial.println("#HARDWARE:" + (String)HARDWARE_NAME);
  #else
    Serial.println("#HARDWARE:GENERIC_ESP32");
  #endif
  return;
}
```

**Android Parser Implementation:**
```kotlin
if (trimmed.startsWith(ProtocolResponses.HARDWARE_PREFIX)) {
    val hardware = trimmed.substringAfter(ProtocolResponses.HARDWARE_PREFIX).trim()
    return MarauderResponse.HardwareInfo(hardware)
}
```

#### 3. Heap Command
**Command:** `heap`  
**Response:** `#HEAP:123456`  
**Purpose:** Query free heap memory in bytes

**Firmware Implementation (esp32_marauder/CommandLine.cpp:302-305):**
```cpp
if (input.startsWith("heap")) {
  Serial.println("#HEAP:" + (String)ESP.getFreeHeap());
  return;
}
```

**Android Parser Implementation:**
```kotlin
if (trimmed.startsWith(ProtocolResponses.HEAP_PREFIX)) {
    val heapStr = trimmed.substringAfter(ProtocolResponses.HEAP_PREFIX).trim()
    val heap = heapStr.toLongOrNull() ?: 0L
    return MarauderResponse.HeapInfo(heap)
}
```

## Android App Implementation

### 1. Data Models (MarauderModels.kt)

#### Response Types
```kotlin
sealed class MarauderResponse {
    // ... existing response types ...
    
    // Protocol responses for seamless communication
    data class VersionInfo(val version: String) : MarauderResponse()
    data class HardwareInfo(val hardware: String) : MarauderResponse()
    data class HeapInfo(val freeHeap: Long) : MarauderResponse()
    data class DeviceVersionInfo(
        val version: String, 
        val hardware: String, 
        val freeHeap: Long
    ) : MarauderResponse()
}
```

#### Device Information
```kotlin
@Serializable
data class DeviceInfo(
    val version: String = "",
    val hardware: String = "",
    val freeHeap: Int = 0,
    val uptime: Long = 0,
    val batteryLevel: Int? = null,
    val temperature: Float? = null
)
```

### 2. Protocol Parser (MarauderProtocolParser.kt)

The parser's `parseLine()` function now checks for protocol responses:

```kotlin
fun parseLine(line: String): MarauderResponse {
    val trimmed = stripAnsi(line).trim()
    
    if (trimmed.isEmpty()) {
        return MarauderResponse.RawOutput(line)
    }
    
    // Check for prompt
    if (PROMPT_PATTERN.matches(trimmed)) {
        return MarauderResponse.Prompt
    }
    
    // Check for protocol responses (version, hardware, heap)
    if (trimmed.startsWith(ProtocolResponses.VERSION_PREFIX)) {
        val version = trimmed.substringAfter(ProtocolResponses.VERSION_PREFIX).trim()
        return MarauderResponse.VersionInfo(version)
    }
    
    if (trimmed.startsWith(ProtocolResponses.HARDWARE_PREFIX)) {
        val hardware = trimmed.substringAfter(ProtocolResponses.HARDWARE_PREFIX).trim()
        return MarauderResponse.HardwareInfo(hardware)
    }
    
    if (trimmed.startsWith(ProtocolResponses.HEAP_PREFIX)) {
        val heapStr = trimmed.substringAfter(ProtocolResponses.HEAP_PREFIX).trim()
        val heap = heapStr.toLongOrNull() ?: 0L
        return MarauderResponse.HeapInfo(heap)
    }
    
    // ... rest of parsing logic ...
}
```

### 3. Repository Integration (MarauderRepository.kt)

#### State Management
```kotlin
private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()
```

#### Protocol Response Handlers
```kotlin
private fun processSerialLine(line: String) {
    // ... other processing ...
    
    val response = parser.parseLine(line)
    
    when (response) {
        is MarauderResponse.VersionInfo -> {
            val current = _deviceInfo.value ?: DeviceInfo()
            _deviceInfo.value = current.copy(version = response.version)
        }
        
        is MarauderResponse.HardwareInfo -> {
            val current = _deviceInfo.value ?: DeviceInfo()
            _deviceInfo.value = current.copy(hardware = response.hardware)
        }
        
        is MarauderResponse.HeapInfo -> {
            val current = _deviceInfo.value ?: DeviceInfo()
            _deviceInfo.value = current.copy(freeHeap = response.freeHeap.toInt())
        }
        
        is MarauderResponse.DeviceVersionInfo -> {
            val current = _deviceInfo.value ?: DeviceInfo()
            _deviceInfo.value = current.copy(
                version = response.version,
                hardware = response.hardware,
                freeHeap = response.freeHeap.toInt()
            )
        }
        
        // ... other handlers ...
    }
}
```

#### Convenience Methods
```kotlin
/**
 * Query device version from firmware
 */
fun getDeviceVersion() {
    sendCommand("version")
}

/**
 * Query device hardware type from firmware
 */
fun getDeviceHardware() {
    sendCommand("hardware")
}

/**
 * Query free heap from firmware
 */
fun getDeviceHeap() {
    sendCommand("heap")
}

/**
 * Query all device information (version, hardware, heap)
 */
fun queryDeviceInfo() {
    getDeviceVersion()
    getDeviceHardware()
    getDeviceHeap()
}
```

## Usage Examples

### Querying Device Information

```kotlin
// In ViewModel or Repository
fun onDeviceConnected() {
    repository.queryDeviceInfo()
}

// Observe device info in UI
repository.deviceInfo.collect { info ->
    info?.let {
        println("Version: ${it.version}")
        println("Hardware: ${it.hardware}")
        println("Free Heap: ${it.freeHeap} bytes")
    }
}
```

### Individual Queries

```kotlin
// Query just version
repository.getDeviceVersion()

// Query just hardware
repository.getDeviceHardware()

// Query just heap
repository.getDeviceHeap()
```

### Testing Protocol Responses

```kotlin
@Test
fun `parse protocol version response`() {
    val line = "#VERSION:v1.9.0"
    val result = parser.parseLine(line)
    assertTrue(result is MarauderResponse.VersionInfo)
    assertEquals("v1.9.0", (result as MarauderResponse.VersionInfo).version)
}

@Test
fun `parse protocol hardware response`() {
    val line = "#HARDWARE:Flipper Zero Dev Board"
    val result = parser.parseLine(line)
    assertTrue(result is MarauderResponse.HardwareInfo)
    assertEquals("Flipper Zero Dev Board", (result as MarauderResponse.HardwareInfo).hardware)
}

@Test
fun `parse protocol heap response`() {
    val line = "#HEAP:123456"
    val result = parser.parseLine(line)
    assertTrue(result is MarauderResponse.HeapInfo)
    assertEquals(123456L, (result as MarauderResponse.HeapInfo).freeHeap)
}
```

## Testing

### Unit Tests
All protocol parsing is covered by unit tests in `MarauderProtocolParserTest.kt`:

- ✅ `parse protocol version response`
- ✅ `parse protocol hardware response`
- ✅ `parse protocol heap response`
- ✅ `parse protocol heap response with invalid number`

**Test Results:** All 13 tests passing (including 4 new protocol tests)

### Integration Testing

1. **Connect to device**
2. **Send protocol commands:**
   ```
   > version
   #VERSION:v1.9.0
   > hardware
   #HARDWARE:Flipper Zero Dev Board
   > heap
   #HEAP:123456
   ```
3. **Verify state updates** in `deviceInfo` StateFlow

## Protocol Format Specification

### Response Format
```
#PREFIX:VALUE
```

Where:
- `#` = Start marker
- `PREFIX` = Command identifier (VERSION, HARDWARE, HEAP)
- `:` = Separator
- `VALUE` = Response data (varies by command)

### Example Responses

| Command | Response | Description |
|---------|----------|-------------|
| `version` | `#VERSION:v1.9.0` | Firmware version string |
| `hardware` | `#HARDWARE:Flipper Zero Dev Board` | Hardware platform name |
| `heap` | `#HEAP:123456` | Free heap in bytes (numeric) |

## Architecture Benefits

### 1. Type Safety
- Sealed class responses provide compile-time type safety
- Parser returns strongly-typed response objects
- No string parsing in business logic

### 2. State Management
- DeviceInfo stored in single StateFlow
- Incremental updates preserve existing data
- Reactive UI updates via Flow collection

### 3. Testability
- Unit tests for all protocol parsing
- Mockable parser interface
- Isolated response handlers

### 4. Extensibility
- Easy to add new protocol commands
- Response types follow sealed class pattern
- Parser logic centralized

### 5. Documentation
- Protocol specification documented
- Examples for all use cases
- Test cases serve as usage examples

## Hardware Support

The protocol works with all supported ESP32 hardware variants:

- Flipper Zero WiFi Dev Board
- M5Stick-C Plus / Plus2
- M5 Cardputer
- Marauder V4/V6/V7/V8
- CYD (Cheap Yellow Display) variants
- ESP32-C5 DevKit
- Generic ESP32 boards

Hardware name mapping defined in `esp32_marauder/configs.h` (lines 42-88).

## Future Enhancements

### Planned Features
1. **Auto-detection on Connect**
   - Automatically query device info on successful connection
   - Cache device info in preferences

2. **Firmware Update UI**
   - Compare current version with bundled firmware
   - One-click OTA update
   - Progress tracking

3. **Hardware-Specific Features**
   - Enable/disable features based on hardware
   - Display hardware-specific help
   - Optimize UI for screen size

4. **Device Health Monitoring**
   - Periodic heap queries
   - Low memory warnings
   - Performance metrics

5. **Protocol Extensions**
   - Battery status: `#BATTERY:85`
   - Temperature: `#TEMP:45.2`
   - Uptime: `#UPTIME:3600`
   - WiFi status: `#WIFI:STA,192.168.1.100`

## Troubleshooting

### Parser Not Recognizing Responses

**Problem:** Protocol responses treated as raw output

**Solution:**
- Verify firmware sends `#PREFIX:VALUE` format exactly
- Check ANSI stripping if using colored output
- Ensure no extra whitespace in prefix

### DeviceInfo Not Updating

**Problem:** State doesn't reflect queried information

**Solution:**
- Verify commands sent correctly
- Check serial connection active
- Confirm parser in `processSerialLine` flow
- Check StateFlow collection in UI

### Tests Failing

**Problem:** Protocol tests fail to parse

**Solution:**
- Check test input format matches firmware output
- Verify `toLongOrNull()` for heap parsing
- Confirm protocol prefixes match SerialProtocol.kt

## References

### Documentation
- `/MarauderController/IMPLEMENTATION_SERIAL_API.md` - Serial API specification
- `/SEAMLESS_PROTOCOL_IMPLEMENTATION.md` - Seamless protocol plan
- `/MarauderController/README.md` - Android app documentation

### Source Files
- `esp32_marauder/CommandLine.cpp` (lines 287-305) - Firmware commands
- `esp32_marauder/configs.h` (lines 35, 42-88) - Version and hardware names
- `MarauderController/app/src/main/java/com/justcallmekoko/maraudercontroller/data/protocol/SerialProtocol.kt` - Protocol constants
- `MarauderController/app/src/main/java/com/justcallmekoko/maraudercontroller/data/protocol/MarauderProtocolParser.kt` - Parser implementation
- `MarauderController/app/src/main/java/com/justcallmekoko/maraudercontroller/data/repository/MarauderRepository.kt` - State management

## Conclusion

The protocol sync implementation provides a robust foundation for seamless ESP32-Android communication. The implementation:

✅ **Complete** - All protocol commands parsed and handled  
✅ **Tested** - 13 unit tests, all passing  
✅ **Type-safe** - Sealed class responses  
✅ **Reactive** - StateFlow for UI updates  
✅ **Extensible** - Easy to add new commands  
✅ **Documented** - Comprehensive documentation and examples  

The firmware and Android app now communicate seamlessly using a well-defined protocol, enabling future enhancements like automatic firmware updates and hardware-specific features.

---

**Implementation Date:** December 27, 2024  
**Version:** 1.0.0  
**Status:** ✅ Complete and Tested
