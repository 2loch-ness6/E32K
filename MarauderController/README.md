# ESP32 Marauder Controller - Android App

A native Android application for controlling ESP32 Marauder devices over USB OTG.

## Features

- **USB Serial Communication**: Direct USB connection to ESP32 Marauder via USB OTG
- **WiFi Scanning**: Scan for access points and stations
- **Attack Operations**: Launch various WiFi attacks (deauth, beacon spam, probe, etc.)
- **List Management**: Manage access points, stations, and SSIDs
- **Terminal Interface**: Send custom commands directly to the device
- **Material Design 3 UI**: Modern, intuitive interface built with Jetpack Compose

## Requirements

- Android device with USB OTG support
- Android 7.0 (API 24) or higher
- USB OTG cable or adapter
- ESP32 Marauder device

## Supported ESP32 USB-to-Serial Chips

The app supports the following USB-to-serial converter chips commonly found on ESP32 development boards:

- CP2102/CP2104 USB to UART Bridge (VID: 0x10C4, PID: 0xEA60)
- CH340 USB to Serial (VID: 0x1A86, PID: 0x7523)
- FTDI FT232R (VID: 0x0403, PID: 0x6001)
- Espressif USB JTAG/Serial Debug Unit (VID: 0x303A, PID: 0x1001)
- Espressif USB Serial JTAG Controller (VID: 0x303A, PID: 0x1002)

## Building the App

### Prerequisites

1. **Android Studio**: Download and install [Android Studio](https://developer.android.com/studio)
2. **JDK 17**: Required for building (included with Android Studio)
3. **Android SDK**: API Level 34 (Android 14)

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/2loch-ness6/E32K.git
   cd E32K/MarauderController
   ```

2. Open the project in Android Studio:
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `E32K/MarauderController` directory

3. Sync Gradle:
   - Android Studio will automatically prompt to sync Gradle
   - Wait for the sync to complete

4. Build the APK:
   ```bash
   ./gradlew assembleDebug
   ```
   
   Or use Android Studio:
   - Select "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"

5. The APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Command Line Build (Alternative)

If you prefer building from the command line without Android Studio:

```bash
cd MarauderController

# Set ANDROID_HOME environment variable (adjust path for your system)
export ANDROID_HOME=$HOME/Android/Sdk  # Linux/Mac
# or
set ANDROID_HOME=%LOCALAPPDATA%\\Android\\Sdk  # Windows

# Build the debug APK
./gradlew assembleDebug

# Build the release APK (requires signing)
./gradlew assembleRelease
```

## Installation

### Install via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Install Directly on Device

1. Copy the APK file to your Android device
2. Enable "Install from Unknown Sources" in device settings
3. Use a file manager to locate and tap the APK file
4. Follow the installation prompts

## Usage

### First Time Setup

1. Connect your ESP32 Marauder device to your Android phone using a USB OTG cable
2. Launch the Marauder Controller app
3. Grant USB permission when prompted
4. The app will automatically detect and list available USB devices
5. Tap "Connect" next to your ESP32 device

### Using the App

#### Connection Screen
- **Scan for Devices**: Refresh the list of connected USB devices
- **Connect**: Establish a serial connection to the selected device
- **Connection Status**: Shows current connection state in the top bar

#### WiFi Tab
- **Scan AP**: Scan for nearby WiFi access points
- **Scan Station**: Scan for WiFi client devices (stations)
- **Stop Scan**: Stop the current scanning operation
- **Access Point List**: View discovered access points with signal strength and encryption

#### Attacks Tab
- **Deauth Attack**: Disconnect clients from selected access points
- **Beacon Spam**: Broadcast fake SSIDs
- **Probe Attack**: Send probe requests
- **Rick Roll**: Special beacon spam attack
- **AP Spam**: Spam access point frames
- **Mimic**: Mimic existing access points
- **Start/Stop**: Control attack operations

#### Lists Tab
- **Access Points**: View and manage scanned access points
- **Stations**: View discovered client devices
- **SSIDs**: Manage SSID list for attacks
- **Select/Deselect**: Choose targets for attacks
- **Add SSID**: Manually add SSIDs to the list
- **Generate Random**: Create random SSIDs
- **Clear**: Clear the lists

#### Settings Tab
- **Channel**: Set WiFi channel (1-14)
- **Device Info**: View ESP32 device information
- **GPS Data**: View GPS information (if GPS module connected)
- **Reboot**: Restart the ESP32 device

#### Terminal
- Tap the terminal icon (top bar) to show/hide the terminal
- View real-time output from the device
- Send custom commands directly to the ESP32 Marauder
- Useful for advanced operations and debugging

## Permissions

The app requires the following permissions:

- **USB Host**: Required for USB OTG communication with ESP32
- **Location**: Required by Android for WiFi scanning (not used for actual location tracking)
- **WiFi State**: To monitor WiFi state changes

## Troubleshooting

### Device Not Detected
1. Ensure your Android device supports USB OTG
2. Try a different USB cable (some cables are charge-only)
3. Make sure the ESP32 is powered on
4. Check if the USB chip on your ESP32 is supported (see list above)

### Connection Fails
1. Grant USB permission when prompted
2. Close and reopen the app
3. Try disconnecting and reconnecting the USB cable
4. Reboot your Android device

### Commands Not Working
1. Ensure you're using compatible ESP32 Marauder firmware
2. Check the terminal for error messages
3. Verify the ESP32 is responding (look for a prompt `>` in terminal)
4. Try sending a simple command like `help` in the terminal

### Build Issues
1. Ensure you have Android SDK installed and ANDROID_HOME set
2. Make sure you have internet connectivity (Gradle needs to download dependencies)
3. Try cleaning the build: `./gradlew clean`
4. Sync Gradle files in Android Studio
5. Check that you have JDK 17 installed

## Architecture

The app is built using modern Android development practices:

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Kotlin Coroutines and Flows
- **USB Serial**: usb-serial-for-android library
- **Build System**: Gradle with Kotlin DSL

### Key Components

- **SerialConnectionManager**: Handles USB serial communication
- **MarauderRepository**: Manages device state and data
- **MarauderProtocolParser**: Parses ESP32 Marauder command responses
- **MarauderViewModel**: Manages UI state and user interactions
- **Compose UI**: Modern declarative UI screens and components

## Development

### Project Structure

```
MarauderController/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/justcallmekoko/maraudercontroller/
│   │   │   │   ├── data/
│   │   │   │   │   ├── protocol/        # Command definitions and parsing
│   │   │   │   │   ├── repository/      # Data repository
│   │   │   │   │   └── serial/          # USB serial communication
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/      # Reusable UI components
│   │   │   │   │   ├── screens/         # App screens
│   │   │   │   │   ├── theme/           # Theme and styling
│   │   │   │   │   └── viewmodel/       # ViewModels
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── MarauderApplication.kt
│   │   │   ├── res/                      # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Adding New Commands

1. Add command definition in `MarauderCommands.kt`
2. Add response parsing in `MarauderProtocolParser.kt`
3. Add repository method in `MarauderRepository.kt`
4. Add ViewModel method in `MarauderViewModel.kt`
5. Update UI to expose the new functionality

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of the E32K ESP32 Marauder project. See the main repository for license information.

## Credits

- ESP32 Marauder firmware by justcallmekoko
- usb-serial-for-android library by mik3y
- Android development by the E32K team

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing issues for solutions
- Consult the ESP32 Marauder documentation

## Disclaimer

This tool is for educational and authorized security testing purposes only. Users are responsible for complying with all applicable laws and regulations. Unauthorized access to computer networks is illegal.
