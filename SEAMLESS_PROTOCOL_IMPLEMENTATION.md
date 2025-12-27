# Seamless Serial Communication Layer Implementation Plan

## Overview
Comprehensive protocol layer for seamless ESP32-Android communication with automatic firmware detection and OTA update capability.

## Phase 1: Protocol Enhancement (COMPLETED)

### ESP32 Firmware Changes
✅ **CommandLine.cpp**
- Added `version` command → Returns `#VERSION:v1.9.0`
- Added `hardware` command → Returns `#HARDWARE:MARAUDER_FLIPPER` (or device type)
- Added `heap` command → Returns `#HEAP:123456` (free heap bytes)

### Android App Protocol Layer
✅ **SerialProtocol.kt** - Created
- Protocol version management
- Command constants
- Response parsing
- Hardware type enumeration
- Version comparison utility

✅ **FirmwareManager.kt** - Created
- Manages bundled firmware binaries
- Hardware-to-firmware mapping
- Checksum verification
- Bootloader/partition support

✅ **FirmwareUpdateManager.kt** - Created
- Version checking
- Update availability detection
- OTA update orchestration
- Progress tracking

## Phase 2: Firmware Packaging (TODO)

### Directory Structure
```
MarauderController/
└── app/
    └── src/
        └── main/
            └── assets/
                └── firmware/
                    ├── flipper/
                    │   ├── esp32_marauder.bin
                    │   ├── bootloader.bin
                    │   └── partitions.bin
                    ├── m5stickc/
                    │   └── esp32_marauder.bin
                    ├── cardputer/
                    │   └── esp32_marauder.bin
                    └── [other hardware variants]/
```

### Build Script Integration
Create `copy_firmware.sh`:
```bash
#!/bin/bash
# Copy pre-built firmware binaries to Android assets

FIRMWARE_SOURCE="../FlashFiles"
FIRMWARE_DEST="app/src/main/assets/firmware"

mkdir -p "$FIRMWARE_DEST"

# Copy Flipper Zero firmware
cp "$FIRMWARE_SOURCE/FlipperZeroDevBoard/"*.bin "$FIRMWARE_DEST/flipper/"

# Copy other hardware variants
# ... (repeat for each hardware type)
```

Add to `build.gradle.kts`:
```kotlin
android {
    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets")
        }
    }
}

tasks.register("copyFirmware", Exec::class) {
    workingDir(rootDir)
    commandLine("sh", "copy_firmware.sh")
}

tasks.named("preBuild") {
    dependsOn("copyFirmware")
}
```

## Phase 3: Protocol Parser Integration (TODO)

### Update MarauderProtocolParser.kt
```kotlin
// Add protocol response handling
when {
    line.startsWith(ProtocolResponses.VERSION_PREFIX) -> {
        val version = line.substringAfter(":")
        MarauderResponse.VersionInfo(version)
    }
    line.startsWith(ProtocolResponses.HARDWARE_PREFIX) -> {
        val hardware = line.substringAfter(":")
        MarauderResponse.HardwareInfo(hardware)
    }
    line.startsWith(ProtocolResponses.HEAP_PREFIX) -> {
        val heap = line.substringAfter(":").toLongOrNull() ?: 0
        MarauderResponse.HeapInfo(heap)
    }
}
```

### Update MarauderRepository.kt
```kotlin
class MarauderRepository(context: Context) {
    // Add firmware managers
    private val firmwareManager = FirmwareManager(context)
    private val updateManager = FirmwareUpdateManager(serialManager, firmwareManager)
    
    // Expose update status
    val updateStatus: StateFlow<UpdateStatus> = updateManager.updateStatus
    val deviceVersion: StateFlow<DeviceVersion?> = updateManager.deviceVersion
    
    // Add firmware check on connection
    private fun onConnected() {
        scope.launch {
            val version = updateManager.checkDeviceVersion()
            if (version != null && updateManager.isUpdateAvailable(version)) {
                // Notify user of available update
            }
        }
    }
}
```

## Phase 4: UI Integration (TODO)

### Settings Screen Enhancement
Add firmware update section:
```kotlin
@Composable
fun FirmwareUpdateSection(viewModel: MarauderViewModel) {
    val updateStatus by viewModel.updateStatus.collectAsState()
    val deviceVersion by viewModel.deviceVersion.collectAsState()
    
    Card {
        Column {
            Text("Firmware Management")
            
            deviceVersion?.let { version ->
                Text("Current: ${version.version}")
                Text("Hardware: ${version.hardware}")
                
                when (val status = updateStatus) {
                    is UpdateStatus.UpdateAvailable -> {
                        Button(onClick = { viewModel.performUpdate() }) {
                            Text("Update to ${status.newVersion}")
                        }
                    }
                    is UpdateStatus.Uploading -> {
                        LinearProgressIndicator(
                            progress = status.progress.toFloat() / status.total
                        )
                        Text("${status.progress}/${status.total} bytes")
                    }
                    else -> { /* Handle other states */ }
                }
            }
        }
    }
}
```

### Connection Screen Auto-Check
```kotlin
// On successful connection
LaunchedEffect(connectionState) {
    if (connectionState is ConnectionState.Connected) {
        viewModel.checkFirmwareVersion()
    }
}
```

## Phase 5: OTA Update Protocol (TODO)

### ESP32 Update Handler
```cpp
// In CommandLine.cpp
if (input.startsWith("update")) {
    if (input.indexOf("start") > 0) {
        // Prepare for OTA update
        Serial.println("#UPDATE:READY");
        startOTAUpdate();
    } else if (input.indexOf("verify") > 0) {
        // Verify update
        if (verifyUpdate()) {
            Serial.println("#UPDATE:SUCCESS");
        } else {
            Serial.println("#UPDATE:ERROR:Verification failed");
        }
    }
    return;
}
```

### OTA Update Implementation
```cpp
// In WiFiScan.cpp or new OTAUpdate.cpp
#include <Update.h>

bool startOTAUpdate() {
    if (!Update.begin(UPDATE_SIZE_UNKNOWN)) {
        return false;
    }
    updateInProgress = true;
    return true;
}

void handleOTAData(uint8_t* data, size_t len) {
    if (Update.write(data, len) != len) {
        Update.abort();
    }
}

bool completeOTAUpdate() {
    if (Update.end(true)) {
        return true;
    }
    return false;
}
```

## Phase 6: Testing & Validation (TODO)

### Test Scenarios
1. **Version Detection**
   - Connect to device
   - Verify version reported correctly
   - Verify hardware type detected

2. **Update Available Check**
   - Mock older firmware version
   - Verify update prompt appears
   - Verify correct firmware selected for hardware

3. **OTA Update Process**
   - Initiate update
   - Monitor progress
   - Verify successful completion
   - Verify device reboots with new version

4. **Error Handling**
   - Connection lost during update
   - Corrupted firmware binary
   - Insufficient heap space
   - Wrong hardware type

### Integration Tests
```kotlin
@Test
fun testVersionComparison() {
    assert(VersionComparator.compare("v1.9.0", "v1.10.0") < 0)
    assert(VersionComparator.compare("v2.0.0", "v1.9.0") > 0)
    assert(VersionComparator.compare("v1.9.0", "v1.9.0") == 0)
}

@Test
fun testFirmwareSelection() {
    val manager = FirmwareManager(context)
    assert(manager.isFirmwareAvailable(HardwareType.FLIPPER))
    assert(manager.getFirmwareVersion() == "v1.9.1")
}
```

## Implementation Status

### Completed ✅
- [x] Protocol command definitions
- [x] ESP32 version/hardware/heap commands
- [x] SerialProtocol.kt with protocol layer
- [x] FirmwareManager.kt for binary management
- [x] FirmwareUpdateManager.kt for OTA orchestration
- [x] Version comparison utility
- [x] Hardware type enumeration

### In Progress 🔄
- [ ] Firmware binary packaging in Android assets
- [ ] Build script integration
- [ ] Protocol parser integration
- [ ] Repository firmware check integration

### Todo 📋
- [ ] OTA update ESP32 implementation
- [ ] UI screens for firmware management
- [ ] Auto-check on connection
- [ ] Progress indicators
- [ ] Error handling
- [ ] Testing suite
- [ ] Documentation

## Usage Example (When Complete)

```kotlin
// User connects to device
viewModel.connect(deviceIndex)

// App automatically checks firmware
viewModel.checkFirmwareVersion()

// If update available, user sees prompt
if (updateAvailable) {
    viewModel.performUpdate() // One-click update
}

// Progress shown in UI
// Device reboots with new firmware
// Connection re-established automatically
```

## Benefits

1. **Seamless Experience**: One-click firmware updates
2. **Always Updated**: App bundles latest firmware
3. **Hardware Aware**: Automatically detects device type
4. **Safe Updates**: Checksum verification
5. **Progress Tracking**: Real-time update status
6. **Error Recovery**: Handles failed updates gracefully

## Next Steps

1. ✅ Complete Phase 1 (Protocol commands) - DONE
2. 🔄 Phase 2: Copy firmware binaries to assets folder
3. 📋 Phase 3: Integrate with parser and repository
4. 📋 Phase 4: Build UI components
5. 📋 Phase 5: Implement ESP32 OTA handler
6. 📋 Phase 6: Test end-to-end

---

**Current Status**: Foundation complete, ready for firmware packaging and integration phases.
