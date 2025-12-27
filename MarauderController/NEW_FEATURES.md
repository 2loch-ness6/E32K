# ESP32 Marauder Controller - New Features Guide

## Overview

This document describes the new features added to the ESP32 Marauder Controller Android app.

## New Features

### 1. Dark/Light Theme Toggle

The app now supports three theme modes:
- **Light Theme**: Always use light colors
- **Dark Theme**: Always use dark colors
- **System Default**: Follow device theme settings

**How to Use:**
1. Go to Settings tab
2. Tap "Change" next to Theme
3. Select your preferred theme mode

### 2. Session Management

Save and restore your scanning sessions with all captured data.

**Features:**
- Save current scan results, SSIDs, and device configuration
- Load previous sessions
- Export sessions to external storage
- Auto-generated session names with timestamps
- Session history with dates

**How to Use:**
- Saving: After scanning, use the session save function
- Loading: Browse saved sessions and load any previous session
- Exporting: Export sessions as JSON files for backup or analysis

### 3. Data Export

Export captured data in multiple formats.

**Supported Formats:**
- **JSON**: Structured data format
- **CSV**: Spreadsheet-compatible format
- **TXT**: Human-readable text format
- **WiGLE**: Standard wardriving format

**Exportable Data:**
- Access points with SSID, BSSID, channel, encryption, signal strength
- Stations (client devices) with MAC addresses and signal info
- Terminal logs (command history and output)
- Packet statistics
- Wardriving data with GPS coordinates

**How to Use:**
1. Capture data (APs, stations, etc.)
2. Go to Export menu
3. Select data type and format
4. Choose export location
5. Tap Export

### 4. Automation & Scripting

Create and run automated command sequences.

**Features:**
- Script editor for creating command sequences
- Configurable delays between commands
- Repeat functionality for continuous operations
- Script library management
- Example scripts included

**Example Scripts:**
- **Quick WiFi Scan**: Scan and list access points
- **Wardrive Mode**: Continuous scanning with GPS tracking
- **Deauth All Nearby**: Scan and attack all detected APs
- **Beacon Spam Random**: Generate random SSIDs and broadcast

**Script Format:**
```json
{
  "name": "Script Name",
  "commands": ["scanap", "list -a"],
  "delay": 2000,
  "repeat": false,
  "repeatCount": 1
}
```

**How to Use:**
1. Go to Scripts menu
2. Create new script or select example
3. Add commands in sequence
4. Set delay between commands
5. Enable repeat if needed
6. Save and run

### 5. Bluetooth Support

Scan and interact with Bluetooth devices (requires ESP32 with BT).

**Features:**
- Bluetooth device scanning
- Device information (name, address, RSSI, manufacturer)
- Service discovery
- Bluetooth device list management

**Requirements:**
- ESP32 board with Bluetooth capability
- Compatible firmware version
- Bluetooth enabled in app settings

**How to Use:**
1. Enable Bluetooth in Settings
2. Go to WiFi/Scan tab
3. Tap "Scan Bluetooth"
4. View discovered devices in Lists tab

### 6. GPS Integration & Wardriving

Enhanced GPS support for wardriving activities.

**Features:**
- GPS tracking during scans
- Location tagging for access points
- WiGLE-format export
- GPS data display (coordinates, satellites, accuracy)

**Wardriving Workflow:**
1. Enable GPS in Settings
2. Connect GPS module to ESP32
3. Start scanning
4. GPS coordinates are automatically recorded
5. Export in WiGLE format for mapping

**WiGLE Export Format:**
```csv
MAC,SSID,AuthMode,FirstSeen,Channel,RSSI,CurrentLatitude,CurrentLongitude,AltitudeMeters,AccuracyMeters,Type
```

### 7. Packet Statistics Visualization

Real-time packet capture analysis.

**Displayed Statistics:**
- Beacon packets
- Probe requests
- Deauthentication packets
- EAPOL (handshake) packets
- Data packets
- Total packet count

**How to View:**
- Statistics are updated in real-time during scans
- Available in the Statistics section
- Export stats for offline analysis

### 8. Custom Attack Configuration

Advanced attack parameter configuration.

**Configurable Parameters:**
- Target MAC address
- Target SSID
- Channel
- Timeout duration
- Random mode
- Attack intensity

**How to Use:**
1. Go to Attacks tab
2. Select attack type
3. Tap "Configure" to set parameters
4. Save configuration as preset
5. Start attack with custom settings

### 9. Multi-Language Support (Prepared for Future)

The app is structured to support multiple languages in the future.

**Currently Supported:**
- English (en) - Default and only language

**Future Plans:**
- Infrastructure is in place for easy addition of more languages
- String resources are externalized and ready for translation
- Language switching functionality will be added when translations are available

**For Developers:**
To add new languages in the future, create `values-{lang}/strings.xml` files with translations.

### 10. Preferences Management

Centralized settings and preferences.

**Available Preferences:**
- Theme mode (Light/Dark/System)
- Auto-connect to last device
- GPS integration enable/disable
- Bluetooth support enable/disable
- Session auto-save
- Export path configuration
- Packet visualization enable/disable
- Scripting enable/disable

**Persistence:**
All preferences are saved using Android DataStore and persist across app restarts.

## Technical Details

### Architecture

The new features follow clean architecture principles:

```
data/
├── preferences/     # DataStore preferences
├── session/         # Session persistence
├── export/          # Data export functionality
├── scripting/       # Script management
└── protocol/        # Data models

ui/
├── screens/         # Enhanced UI screens
└── viewmodel/       # State management
```

### Data Persistence

- **Preferences**: DataStore (key-value pairs)
- **Sessions**: JSON files in app private storage
- **Scripts**: JSON files in app private storage
- **Exports**: User-selected external storage

### New Data Models

```kotlin
// Bluetooth device
data class BluetoothDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val type: String,
    val services: List<String>,
    val manufacturer: String
)

// Script configuration
data class ScriptConfig(
    val name: String,
    val commands: List<String>,
    val delay: Int,
    val repeat: Boolean,
    val repeatCount: Int
)

// Session data
data class SessionData(
    val timestamp: Long,
    val deviceInfo: DeviceInfo?,
    val accessPoints: List<AccessPoint>,
    val stations: List<Station>,
    val ssids: List<SSID>,
    val gpsData: GpsData?,
    val packetStats: PacketStats?,
    val terminalLog: List<String>
)
```

### API Extensions

New ViewModel functions:
- `setThemeMode(mode: ThemeMode)`
- `setAutoConnect(enabled: Boolean)`
- `setGpsEnabled(enabled: Boolean)`
- `setBluetoothEnabled(enabled: Boolean)`
- Session management functions
- Export functions
- Script execution functions

## Permissions

Additional permissions may be required:

```xml
<!-- Location for GPS and Bluetooth scanning -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Storage for exports -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## Usage Examples

### Example 1: Complete Wardriving Session

```
1. Enable GPS in Settings
2. Start "Wardrive Mode" script
3. Drive/walk around area
4. Stop script after desired area covered
5. Export data in WiGLE format
6. Upload to WiGLE.net for mapping
```

### Example 2: Automated Security Testing

```
1. Create custom script:
   - scanap (scan access points)
   - select -a (select all)
   - attack -t deauth (deauth attack)
   - wait 30s
   - stopscan
2. Set repeat to 5 times
3. Run script
4. Save session when complete
5. Export terminal log for report
```

### Example 3: Bluetooth Device Survey

```
1. Enable Bluetooth in Settings
2. Scan Bluetooth devices
3. View discovered devices in Lists
4. Export device list as CSV
5. Analyze manufacturer distribution
```

## Troubleshooting

### GPS Not Working
- Ensure GPS module is connected to ESP32
- Check ESP32 firmware supports GPS
- Enable location permissions in Android
- Wait for GPS fix (may take 1-2 minutes outdoors)

### Bluetooth Not Scanning
- Verify ESP32 has Bluetooth capability
- Enable Bluetooth in app settings
- Grant Bluetooth permissions
- Check firmware supports BT commands

### Exports Failing
- Verify storage permissions granted
- Check export path is writable
- Ensure sufficient storage space
- Try different export location

### Scripts Not Running
- Verify device is connected
- Check command syntax
- Ensure adequate delays between commands
- Review terminal output for errors

## Performance Notes

- Session files are stored in app private storage (no storage limits)
- Export files location is user-configurable
- Large sessions (1000+ APs) may take a few seconds to save/load
- Script execution is asynchronous and non-blocking
- Theme changes apply immediately without restart

## Security Considerations

- Session data stored securely in app private storage
- Export files should be handled carefully (may contain network info)
- Scripting should be used responsibly
- Always comply with local laws regarding wireless testing
- Wardriving data may contain location information

## Future Enhancements

Potential future additions:
- Map visualization for wardriving
- Cloud sync for sessions
- More export formats (KML, GPX)
- Visual script builder
- Packet capture analysis tools
- Attack effectiveness metrics
- Multi-device support
- Remote control via network

## Contributing

To add new features or translations:
1. Fork the repository
2. Create feature branch
3. Add functionality following existing patterns
4. Add string resources for all languages
5. Test thoroughly
6. Submit pull request

## Support

For issues or questions:
- GitHub Issues: Report bugs and request features
- Documentation: Check README and QUICKSTART
- ESP32 Marauder Docs: For firmware-specific questions

---

**Version**: 1.1.0  
**Last Updated**: December 2024  
**Minimum Android Version**: 7.0 (API 24)  
**Target Android Version**: 14 (API 34)
