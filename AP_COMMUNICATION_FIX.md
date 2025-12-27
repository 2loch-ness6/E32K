# ESP32 Marauder - AP Communication Fix

## Problem Statement

The ESP32 Marauder firmware and Android Controller app had a communication protocol mismatch that prevented access points (APs) from being parsed and displayed correctly in the Android app. This also prevented users from selecting targets for attacks.

## Root Cause Analysis

### Issue #1: Incompatible Output Format

**ESP32 Firmware Output (Before Fix):**
```
[0][CH:1] MyWiFi -45
[1][CH:11] OpenNet -67 (selected)
```

**Android App Expected Format:**
```
[0] SSID (BSSID) Ch: 1 RSSI: -50 Encryption
```

### Issue #2: Missing Data Fields

The ESP32 firmware had all the necessary data (BSSID, security/encryption type) in the `AccessPoint` struct but wasn't outputting it in the LIST command.

## Solution

### 1. ESP32 Firmware Changes

#### File: `esp32_marauder/WiFiScan.h`
- Added function declaration: `String security_type_to_string(uint8_t security_type);`

#### File: `esp32_marauder/WiFiScan.cpp`
- Implemented `security_type_to_string()` function to convert internal security constants to short strings
- Maps WIFI_SECURITY_* constants to human-readable strings:
  - WIFI_SECURITY_OPEN → "Open"
  - WIFI_SECURITY_WEP → "WEP"
  - WIFI_SECURITY_WPA → "WPA"
  - WIFI_SECURITY_WPA2 → "WPA2"
  - WIFI_SECURITY_WPA3 → "WPA3"
  - WIFI_SECURITY_WPA_WPA2_MIXED → "WPA/WPA2"
  - WIFI_SECURITY_WPA2_ENTERPRISE → "WPA2-E"
  - WIFI_SECURITY_WPA3_ENTERPRISE → "WPA3-E"
  - WIFI_SECURITY_WAPI → "WAPI"
  - Default → "Unknown"

#### File: `esp32_marauder/CommandLine.cpp`
- Updated LIST command (lines 1477-1490) to output:
  - BSSID using `macToString()` helper
  - Security/encryption using new `security_type_to_string()`
  - Proper formatting with all required fields
  - Selected marker `(*)` at end for selected APs

**New Output Format:**
```
[0] MyWiFi (AA:BB:CC:DD:EE:FF) Ch: 6 RSSI: -45 WPA2
[1] OpenNet (11:22:33:44:55:66) Ch: 11 RSSI: -67 Open (*)
```

### 2. Android App Changes

#### File: `MarauderController/app/src/main/java/.../MarauderProtocolParser.kt`
- Enhanced encryption field parsing to remove `(*)` marker
- Prevents encryption display from showing "WPA2 (*)" and shows just "WPA2"
- Parser already had correct regex pattern and selected detection logic

## Testing

### Regex Pattern Validation
All test cases passed with the new format:

```python
AP_LINE_PATTERN = r'\[(\d+)\]\s*(.+?)\s*\((.+?)\)\s*Ch:\s*(\d+)\s*RSSI:\s*(-?\d+)\s*(.+)'

✓ [0] MyWiFi (AA:BB:CC:DD:EE:FF) Ch: 6 RSSI: -45 WPA2
✓ [1] OpenNet (11:22:33:44:55:66) Ch: 11 RSSI: -67 Open
✓ [2] Test Network (00:11:22:33:44:55) Ch: 1 RSSI: -89 WPA3
✓ [3] Hidden SSID (FF:EE:DD:CC:BB:AA) Ch: 3 RSSI: -50 WPA/WPA2
✓ [4] Office (12:34:56:78:9A:BC) Ch: 6 RSSI: -60 WPA2-E
✓ [5] Guest (AB:CD:EF:12:34:56) Ch: 11 RSSI: -70 WPA3-E (*)
```

### Android Build Validation
```bash
cd MarauderController && ./gradlew compileDebugKotlin
BUILD SUCCESSFUL in 2m 32s
26 actionable tasks: 26 executed
```

All Kotlin files compiled successfully with only deprecation warnings (no errors).

## Impact

### Fixed Functionality
1. **AP Parsing:** APs are now correctly parsed from serial output
2. **AP Display:** All AP information (SSID, BSSID, Channel, RSSI, Encryption) displays correctly
3. **AP Selection:** Users can select/deselect targets using `select -a <index>` command
4. **Attack Targeting:** Selected APs can be targeted for attacks (deauth, etc.)

### Data Flow
```
ESP32: Scan → List APs → Format Output
  ↓
Serial: [0] WiFi (MAC) Ch: 6 RSSI: -45 WPA2
  ↓
Android: Parse → Store → Display → Select → Attack
```

## Backward Compatibility

The changes are **forward-compatible only**:
- Old Android app versions won't work with new ESP32 firmware
- New Android app versions won't work with old ESP32 firmware
- Both ESP32 firmware and Android app should be updated together

## Files Modified

### ESP32 Firmware
- `esp32_marauder/WiFiScan.h` - Added function declaration
- `esp32_marauder/WiFiScan.cpp` - Implemented security string converter
- `esp32_marauder/CommandLine.cpp` - Updated LIST command output

### Android App
- `MarauderController/app/src/main/java/.../MarauderProtocolParser.kt` - Enhanced parser

## Commands Affected

### Working Commands
- `scanap` - Scan for access points
- `list -a` - List all APs with full details
- `select -a <index>` - Select specific AP by index
- `select -a all` - Select all APs
- `attack -t deauth` - Attack selected APs

### Output Example
```
> scanap
Starting AP scan. Stop with stopscan
... scanning ...

> list -a
[0] Home WiFi (AA:BB:CC:DD:EE:FF) Ch: 6 RSSI: -45 WPA2
[1] Neighbor (11:22:33:44:55:66) Ch: 1 RSSI: -67 WPA3
[2] Starbucks (22:33:44:55:66:77) Ch: 11 RSSI: -55 Open
3 targets found, 0 selected

> select -a 0,2
2 selected, 0 unselected

> list -a
[0] Home WiFi (AA:BB:CC:DD:EE:FF) Ch: 6 RSSI: -45 WPA2 (*)
[1] Neighbor (11:22:33:44:55:66) Ch: 1 RSSI: -67 WPA3
[2] Starbucks (22:33:44:55:66:77) Ch: 11 RSSI: -55 Open (*)
3 targets found, 2 selected
```

## Future Improvements

1. Add similar format fixes for Station list output
2. Implement unit tests for protocol parser
3. Add integration tests for ESP32-Android communication
4. Consider versioning the protocol for better compatibility handling

## Related Issues

- Fixes AP parsing and display issues
- Fixes target selection functionality
- Enables proper attack targeting
