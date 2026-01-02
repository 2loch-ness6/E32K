# ESP32 Flasher Implementation

This directory contains the complete ESP32 firmware flashing implementation for the E32K/MarauderController Android app.

## Overview

The ESP32 flasher enables the Android app to flash firmware directly to ESP32 devices via USB serial connection, implementing the same protocol used by esptool.py.

## Components

### 1. SLIP Protocol (`SlipProtocol.kt`)
Implementation of RFC 1055 Serial Line Internet Protocol for reliable packet framing.

**Features:**
- Encodes data with proper escape sequences
- Decodes SLIP-encoded packets
- Handles special characters (END, ESC, ESC_END, ESC_ESC)
- Exception handling for invalid sequences

**Test Coverage:** 9 comprehensive test cases in `SlipProtocolTest.kt`

### 2. Boot Mode Controller (`BootModeController.kt`)
Controls ESP32 boot mode entry using DTR/RTS hardware flow control signals.

**Supported Variants:**
- GENERIC - Standard NodeMCU/Wemos style boards
- DEVKITC - Espressif DevKitC (inverted logic)
- WROOM - WROOM modules (longer delays)
- S2 - ESP32-S2 specific sequence
- S3 - ESP32-S3 specific sequence
- C3 - ESP32-C3 specific sequence

**Features:**
- Automatic bootloader entry for different board types
- Hard reset capability
- Normal mode reset
- Configurable timing delays

### 3. Boot Mode Detector (`BootModeDetector.kt`)
Auto-detects the correct boot sequence by trying different variants.

**Features:**
- Iterates through common board variants
- Returns best matching variant
- Falls back to GENERIC if detection fails

### 4. ESP32 Commands (`Esp32Commands.kt`)
Implementation of ESP32 ROM bootloader command protocol.

**Implemented Commands:**
- `ESP_SYNC` (0x08) - Establish communication
- `ESP_FLASH_BEGIN` (0x02) - Prepare flash operation
- `ESP_FLASH_DATA` (0x03) - Write flash data block
- `ESP_FLASH_END` (0x04) - Finish flash operation
- `ESP_CHANGE_BAUDRATE` (0x0f) - Change serial speed
- Additional commands defined for future use

**Features:**
- Command packet structure with checksums
- Response packet parsing
- Little-endian byte order handling
- Status code interpretation

### 5. ESP32 Flasher (`Esp32Flasher.kt`)
Main flasher implementation with progress tracking.

**Flash Stages:**
1. ENTERING_BOOTLOADER - Putting ESP32 into bootloader mode
2. SYNCING - Establishing communication
3. PREPARING_FLASH - Sending flash begin command
4. WRITING_FLASH - Writing firmware blocks
5. VERIFYING - Optional verification step
6. FINISHING - Completing flash operation
7. COMPLETE - Success
8. ERROR - Failure state

**Features:**
- Asynchronous flashing with Kotlin coroutines
- Real-time progress tracking via StateFlow
- Block-by-block writing with padding
- Configurable flash address and verification
- Retry logic for sync operation
- Timeout handling for all operations

**Progress Tracking:**
```kotlin
data class FlashProgress(
    val stage: FlashStage,
    val bytesWritten: Int,
    val totalBytes: Int,
    val currentBlock: Int,
    val totalBlocks: Int,
    val message: String,
    val percentage: Float
)
```

## Usage Example

```kotlin
// Initialize components
val serialPort: UsbSerialPort = ... // Obtain from SerialConnectionManager
val bootController = BootModeController(serialPort)
val flasher = Esp32Flasher(serialPort, bootController)

// Observe progress
flasher.progress.collect { progress ->
    println("Stage: ${progress.stage}, Progress: ${progress.percentage}%")
    println(progress.message)
}

// Flash firmware
val firmware = loadFirmwareBytes() // Load firmware binary
val result = flasher.flashFirmware(
    firmware = firmware,
    address = 0x10000, // App partition address
    verify = true
)

if (result.isSuccess) {
    println("Flash successful!")
} else {
    println("Flash failed: ${result.exceptionOrNull()?.message}")
}
```

## Protocol Details

### SLIP Framing
All commands and responses are wrapped in SLIP frames:
```
[END] [packet data] [END]
```

Special bytes in packet data are escaped:
- `0xC0` (END) → `0xDB 0xDC` (ESC + ESC_END)
- `0xDB` (ESC) → `0xDB 0xDD` (ESC + ESC_ESC)

### Command Packet Structure
```
Byte 0:    Direction (0x00 = request, 0x01 = response)
Byte 1:    Command opcode
Bytes 2-3: Data size (little endian)
Bytes 4-7: Checksum (XOR of data bytes, starting with 0xEF)
Bytes 8+:  Data payload
```

### Flash Block Size
- Default: 1024 bytes (0x400)
- Blocks are padded with 0xFF if needed

### Typical Flash Sequence
1. Enter bootloader mode (DTR/RTS manipulation)
2. SYNC command (retry up to 7 times)
3. FLASH_BEGIN (total size, block count, block size, offset)
4. FLASH_DATA (repeat for each block)
5. FLASH_END (reboot flag)

## Testing

### Unit Tests
Run SLIP protocol tests:
```bash
cd MarauderController
./gradlew test
```

### Integration Testing
Integration testing requires actual ESP32 hardware:
1. Connect ESP32 to Android device via USB
2. Grant USB permissions
3. Use flasher API to flash test firmware
4. Verify device boots with new firmware

## Dependencies

- **usb-serial-for-android** - USB serial communication
- **Kotlin Coroutines** - Asynchronous operations
- **Kotlin Flow** - Progress tracking

## Technical Notes

### Timing Considerations
- Boot mode entry delays are critical (100ms typical)
- Sync retries use 100ms delays
- Block write timeout: 5000ms
- Response read timeout: 1000-5000ms depending on operation

### Error Handling
- All operations return `Result<Unit>` for proper error propagation
- `SlipDecodeException` for invalid SLIP frames
- `BootModeException` for boot mode failures
- `IOException` for communication errors

### Memory Efficiency
- Streaming flash writes (block by block)
- Fixed 4KB read buffer
- ByteBuffer for efficient packet construction

## Future Enhancements

Potential additions not yet implemented:
- Flash verification via MD5 checksum (ESP_SPI_FLASH_MD5)
- Memory operations (ESP_MEM_BEGIN, ESP_MEM_DATA, ESP_MEM_END)
- Register read/write (ESP_READ_REG, ESP_WRITE_REG)
- Baudrate change for faster flashing
- Stub loader upload for improved performance
- Multi-partition flashing (bootloader, partitions, app)
- Automatic firmware type detection
- Progress persistence across app restarts

## References

- [esptool.py](https://github.com/espressif/esptool) - Official ESP32 flashing tool
- [RFC 1055](https://tools.ietf.org/html/rfc1055) - SLIP Protocol Specification
- [ESP32 Technical Reference Manual](https://www.espressif.com/sites/default/files/documentation/esp32_technical_reference_manual_en.pdf)
- [ESP-IDF Programming Guide](https://docs.espressif.com/projects/esp-idf/en/latest/)

## License

This implementation is part of the E32K project and follows the project's license terms.
