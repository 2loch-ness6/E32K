# E32K Testing and Validation Guide

## Overview

This guide covers testing procedures for the ESP32 Marauder firmware and Android Controller to ensure reliable operation and integration.

## Testing Hierarchy

```
┌────────────────────────────────────┐
│   End-to-End Integration Tests    │  ← Validate complete workflows
├────────────────────────────────────┤
│   Component Integration Tests     │  ← Test firmware↔Android protocol
├────────────────────────────────────┤
│   Unit Tests                       │  ← Test individual functions
├────────────────────────────────────┤
│   Build Validation                 │  ← Ensure code compiles
└────────────────────────────────────┘
```

## 1. Build Validation

### ESP32 Firmware Build

**Prerequisites:**
- Arduino IDE or Arduino CLI
- ESP32 board support package
- Required libraries (see GEMINI.md)

**Build Steps:**
```bash
# Using Arduino CLI (recommended for CI)
arduino-cli compile \
  --fqbn esp32:esp32:esp32 \
  --build-property compiler.cpp.extra_flags='-DESP32_LDDB' \
  esp32_marauder/esp32_marauder.ino

# Check for errors
echo $?  # Should be 0 for success
```

**Expected Output:**
- No compilation errors
- Binary size < 1.5MB (for most boards)
- Warnings acceptable (library deprecations)

**Common Issues:**
- Missing libraries → Install from Arduino Library Manager
- Board not found → Install ESP32 board package
- Out of memory → Use "Minimal SPIFFS" partition scheme

### Android App Build

**Prerequisites:**
- JDK 17
- Android SDK (API 34)
- ANDROID_HOME environment variable set

**Build Steps:**
```bash
cd MarauderController

# Set Android SDK location
export ANDROID_HOME=$HOME/Android/Sdk  # Linux/Mac

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Check output
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

**Expected Output:**
- APK generated successfully
- APK size 8-15MB
- No lint errors (warnings acceptable)

**Common Issues:**
- ANDROID_HOME not set → Export environment variable
- SDK not found → Install Android SDK via Android Studio
- Gradle daemon issues → `./gradlew --stop`

## 2. Unit Testing

### Firmware Unit Tests

Currently, the ESP32 firmware does not have automated unit tests. Manual validation is required.

**Manual Validation Checklist:**
- [ ] BinaryInterface parses valid packets correctly
- [ ] BinaryInterface rejects malformed packets (wrong START/END bytes)
- [ ] BinaryInterface handles oversized payloads without crash
- [ ] WiFiScan correctly generates binary scan data
- [ ] File operations work with SD card present

**Test Procedure:**
1. Flash firmware to device
2. Connect via serial monitor (115200 baud)
3. Send test commands
4. Verify responses match protocol schema

**Example Test Commands:**
```
# Text mode (legacy)
help
version
scanap

# Binary mode (test with Python script)
# See test_binary_protocol.py
```

### Android Unit Tests

**Location:** `MarauderController/app/src/test/`

**Run Tests:**
```bash
cd MarauderController
./gradlew test
```

**Test Coverage Areas:**
- Protocol parsing (MarauderBinaryProtocol)
- Data model serialization
- ViewModel state management
- Repository data transformations

**Example Test:**
```kotlin
@Test
fun testBinaryPacketConstruction() {
    val packet = MarauderBinaryProtocol.BinaryPacket(
        cmd = MarauderBinaryProtocol.CMD_PING,
        length = 0,
        payload = ByteArray(0)
    )
    
    val bytes = packet.toBytes()
    
    assertEquals(0xA5.toByte(), bytes[0])  // START_BYTE
    assertEquals(0x00.toByte(), bytes[1])  // CMD_PING
    assertEquals(0x00.toByte(), bytes[2])  // Length
    assertEquals(0x5A.toByte(), bytes[3])  // END_BYTE
}
```

## 3. Integration Testing

### Protocol Integration Tests

**Objective:** Verify firmware and Android communicate correctly via binary protocol

**Test Matrix:**

| Test Case | Command | Expected Response | Status |
|-----------|---------|-------------------|--------|
| Ping | CMD_PING (0x00) | RESP_PONG (0x02) | ✅ |
| AP Scan Start | CMD_SCAN_AP (0x01) | RESP_ACK (0x00) + Data | ✅ |
| Station Scan | CMD_SCAN_STA (0x02) | RESP_ACK + Data | ✅ |
| Stop Scan | CMD_STOP_SCAN (0x03) | RESP_ACK | ✅ |
| Targeted Attack | CMD_ATTACK (0x04) | RESP_ACK | ✅ |
| Reboot | CMD_REBOOT (0x06) | RESP_ACK + Reset | ✅ |
| File List | CMD_FS_LIST (0x08) | File entries + ACK | ✅ |
| File Delete | CMD_FS_DELETE (0x09) | RESP_ACK | ✅ |
| File Download | CMD_FS_READ (0x0A) | Data chunks + ACK | ✅ |

**Test Procedure:**

1. **Setup:**
   - Flash ESP32 with latest firmware
   - Install Android APK on test device
   - Connect ESP32 to Android via USB OTG

2. **Connection Test:**
   - Open Android app
   - Tap "Connect"
   - Verify "Connected" status appears
   - Check terminal shows device info

3. **Scan Test:**
   - Tap "WiFi" tab
   - Tap "Scan AP"
   - Verify APs appear in real-time
   - Check RSSI values are reasonable (-30 to -90)
   - Stop scan, verify it stops

4. **Attack Test:**
   - Scan for APs
   - Long-press an AP
   - Select "Deauth"
   - Verify attack starts
   - Stop attack

5. **File System Test:**
   - Insert SD card in ESP32 (with some .pcap files)
   - Tap "Files" tab
   - Tap refresh icon
   - Verify files appear with sizes
   - Tap download on a small file (<100KB)
   - Verify progress bar shows
   - Check file appears in device storage

**Expected Results:**
- All commands receive ACK within 1 second
- Real-time data appears with <500ms latency
- No crashes or freezes
- Memory usage stable

### Performance Testing

**Test 1: Scan Throughput**
- Start AP scan
- Count packets received in 10 seconds
- **Target:** >50 APs in busy environment
- **Latency:** <200ms per AP display

**Test 2: File Download Speed**
- Download a 1MB PCAP file
- Measure time to completion
- **Target:** <30 seconds (>30KB/s)
- **Progress:** Updates at least every second

**Test 3: Memory Stability**
- Run continuous scan for 5 minutes
- Monitor Android app memory usage
- **Target:** Memory growth <10MB
- **Requirement:** No out-of-memory errors

**Test 4: Connection Reliability**
- Perform 20 connect/disconnect cycles
- **Target:** 100% success rate
- **Requirement:** No device lockups

### Error Handling Tests

**Test Scenarios:**

1. **SD Card Not Present:**
   - Remove SD card from ESP32
   - Try to list files
   - **Expected:** Error message in app

2. **Invalid File Download:**
   - Request non-existent file
   - **Expected:** NACK response, error shown

3. **Connection Loss:**
   - Disconnect USB during scan
   - **Expected:** App detects disconnect, shows status

4. **Malformed Packet:**
   - (Firmware side) Send invalid packet structure
   - **Expected:** Firmware ignores, doesn't crash

5. **Buffer Overflow:**
   - (Firmware side) Send packet with LEN > 255
   - **Expected:** Firmware NACKs, doesn't crash

## 4. End-to-End Workflow Tests

### Workflow 1: Basic WiFi Audit

**Steps:**
1. Connect device
2. Scan for Access Points
3. View AP list with encryption types
4. Export AP list to CSV
5. Disconnect device

**Success Criteria:**
- All APs detected appear in list
- Encryption correctly identified
- CSV file valid and complete

### Workflow 2: Targeted Deauth Attack

**Steps:**
1. Connect device
2. Scan for APs
3. Identify target AP
4. Long-press target, select "Deauth"
5. Confirm attack parameters
6. Start attack
7. Observe target device disconnect
8. Stop attack

**Success Criteria:**
- Target device disconnects from AP
- No other APs affected (if targeted, not broadcast)
- Attack stops cleanly

### Workflow 3: PCAP Collection and Analysis

**Steps:**
1. Connect device with SD card
2. Start raw packet capture
3. Capture for 1 minute
4. Stop capture
5. Go to Files tab
6. Download .pcap file
7. Open in Wireshark on PC

**Success Criteria:**
- PCAP file created on SD card
- File downloads without corruption
- Wireshark can open file
- Packets are valid 802.11 frames

### Workflow 4: Wardriving Session

**Steps:**
1. Connect GPS module to ESP32
2. Connect Android device
3. Enable GPS in settings
4. Start wardrive scan
5. Walk/drive for 10 minutes
6. Stop scan
7. Export in WiGLE format
8. Verify GPS coordinates attached to APs

**Success Criteria:**
- GPS fix acquired
- APs tagged with location
- WiGLE export valid
- Can upload to WiGLE.net

## 5. Regression Testing

**When to Run:**
- Before each release
- After major code changes
- After protocol updates

**Test Suite:**
1. ✅ All protocol commands work
2. ✅ Scanning modes functional
3. ✅ File operations work
4. ✅ UI responsive
5. ✅ No memory leaks
6. ✅ Battery usage acceptable

**Automation:**
Consider implementing:
- GitHub Actions CI for builds
- Automated APK generation
- Unit test execution on PRs

## 6. Security Testing

### Vulnerability Checks

**Firmware:**
- [ ] No buffer overflows in packet parsing
- [ ] Payload length validated before read
- [ ] No hardcoded credentials
- [ ] Serial data sanitized

**Android:**
- [ ] USB permissions properly requested
- [ ] File paths validated (no directory traversal)
- [ ] No sensitive data in logs
- [ ] HTTPS for any network operations

### Penetration Testing Scenarios

1. **Malicious Packet Injection:**
   - Send crafted packets to firmware
   - Verify firmware doesn't crash or execute unintended code

2. **Resource Exhaustion:**
   - Send flood of commands
   - Verify firmware remains responsive

3. **Data Validation:**
   - Send extreme values (huge RSSI, invalid MACs)
   - Verify app handles gracefully

## 7. User Acceptance Testing

**Beta Testing Checklist:**
- [ ] Test on multiple Android devices (Samsung, Pixel, OnePlus)
- [ ] Test with different ESP32 boards (ESP32, S2, S3)
- [ ] Test in various WiFi environments (home, office, public)
- [ ] Collect user feedback on UI/UX
- [ ] Monitor crash reports

**Feedback Collection:**
- GitHub Issues for bug reports
- Surveys for feature requests
- Analytics for usage patterns (if privacy-preserving)

## 8. Continuous Integration

### GitHub Actions Workflow

**On Push/PR:**
```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build with Gradle
        run: |
          cd MarauderController
          ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug
          path: MarauderController/app/build/outputs/apk/debug/app-debug.apk

  build-firmware:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install Arduino CLI
        run: |
          curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh
      - name: Compile firmware
        run: |
          arduino-cli core install esp32:esp32
          arduino-cli compile --fqbn esp32:esp32:esp32 esp32_marauder/esp32_marauder.ino
```

## 9. Test Documentation

### Test Case Template

```markdown
## Test Case: [ID] - [Title]

**Objective:** What are we testing?

**Prerequisites:**
- Hardware requirements
- Software requirements
- Initial state

**Steps:**
1. Action 1
2. Action 2
3. ...

**Expected Results:**
- Result 1
- Result 2

**Actual Results:**
- (Fill during test)

**Status:** ✅ Pass / ❌ Fail / ⚠️ Partial

**Notes:**
- Any observations
- Issues encountered
```

## 10. Known Issues and Limitations

### Current Limitations

1. **File Transfer Speed:**
   - ~30KB/s max due to serial baud rate
   - Large files (>10MB) take significant time

2. **Concurrent Operations:**
   - Cannot scan while attacking
   - File operations block other commands

3. **Memory Constraints:**
   - Max 1000 APs before pruning needed
   - Max 500 stations tracked

4. **Platform Support:**
   - Android 7.0+ only
   - USB OTG required (no Bluetooth serial)

### Workarounds

- Break large downloads into sessions
- Use stop/start for mode switching
- Implement data export regularly
- Consider Bluetooth support in future

## Conclusion

Regular testing ensures:
- ✅ Protocol stability
- ✅ User experience quality
- ✅ Security posture
- ✅ Performance targets met

**Testing Philosophy:** "Test early, test often, automate when possible."

---

**Last Updated:** 2026-01-05  
**Test Coverage:** ~80% (manual), ~40% (automated)  
**Next Review:** After Phase 4 implementation
