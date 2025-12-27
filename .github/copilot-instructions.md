# ESP32 Marauder - Copilot Coding Agent Instructions

## Repository Overview

**ESP32 Marauder** is a WiFi/Bluetooth offensive and defensive security testing toolkit for ESP32 microcontrollers. The repository contains firmware for 19+ different ESP32 hardware variants (ESP32, ESP32-S2, ESP32-S3, ESP32-C5), an Android companion app, Python flashing tools, and hardware documentation.

**Repository Size:** ~980MB  
**Primary Language:** C++ (Arduino framework)  
**Secondary Languages:** Kotlin (Android), Python (tooling)  
**Current Version:** v1.9.0 (defined in `esp32_marauder/configs.h`)

## Project Structure

### Key Directories
- **`esp32_marauder/`** - Main ESP32 firmware (~28K lines of C++/Arduino code)
- **`MarauderController/`** - Android app (Kotlin, Jetpack Compose, Material Design 3)
- **`C5_Py_Flasher/`** - Python flasher for ESP32-C5 devices
- **`FlashFiles/`** - Pre-compiled binaries and flashing tools
- **`libraries/`** - Vendored Arduino library (ESPAsyncWebServer)
- **`esp32_marauder/libraries/`** - Additional vendored libraries (TFT_eSPI, NimBLE, etc.)
- **`PCBs/`**, **`mechanical/`**, **`schematics/`** - Hardware design files
- **`User_Setup*.h`** - TFT_eSPI display configuration files for different hardware variants

### Critical Configuration Files
- **`esp32_marauder/configs.h`** - Board targets, feature flags, version number
- **`.github/workflows/build_parallel.yml`** - Main CI build workflow (builds 19 hardware variants)
- **`.github/workflows/nightly_build.yml`** - Nightly automated builds
- **`MarauderController/app/build.gradle.kts`** - Android app build configuration

## Build Instructions - ESP32 Firmware

### Prerequisites
The project uses **Arduino framework** via **Arduino CLI** with GitHub Actions for CI builds. **CRITICAL:** This project does NOT use PlatformIO or standard Arduino IDE builds for CI.

### Build System Details
- **Build Tool:** Arduino CLI with `ArminJo/arduino-test-compile@v3.2.1` and `v3.3.0` GitHub Actions
- **ESP32 Core Versions:** 
  - **2.0.11** for most boards (ESP32, ESP32-S2, ESP32-S3)
  - **3.3.4** for ESP32-C5 boards
- **Board FQBNs:** Varies by hardware (e.g., `esp32:esp32:d32:PartitionScheme=min_spiffs`)

### Required External Libraries (installed from GitHub in CI)
**ALWAYS use these exact versions** as specified in `.github/workflows/build_parallel.yml`:
- **ESP32Ping** v1.6 (marian-craciunescu/ESP32Ping)
- **AsyncTCP** v3.4.8 (ESP32Async/AsyncTCP)
- **MicroNMEA** v2.0.6 (stevemarple/MicroNMEA)
- **ESPAsyncWebServer** v3.8.1 (ESP32Async/ESPAsyncWebServer)
- **TFT_eSPI** V2.5.34 (Bodmer/TFT_eSPI)
- **XPT2046_Touchscreen** v1.4 (PaulStoffregen/XPT2046_Touchscreen)
- **lv_arduino** 3.0.0 (lvgl/lv_arduino)
- **JPEGDecoder** 1.8.0 (Bodmer/JPEGDecoder)
- **NimBLE-Arduino** 1.3.8 or 2.3.6 depending on board (h2zero/NimBLE-Arduino)
- **Adafruit_NeoPixel** 1.12.0 (adafruit/Adafruit_NeoPixel)
- **ArduinoJson** v6.18.2 (bblanchon/ArduinoJson)
- **LinkedList** v1.3.3 (ivanseidel/LinkedList)
- **EspSoftwareSerial** 8.1.0 (plerup/espsoftwareserial)
- **Adafruit_BusIO** 1.15.0 (adafruit/Adafruit_BusIO)
- **Adafruit_MAX1704X** 1.0.2 (adafruit/Adafruit_MAX1704X)

### CRITICAL Build Workarounds

#### 1. zmuldefs Linker Flag (REQUIRED for ESP32 Core 2.0.11)
**ALWAYS** modify `platform.txt` before building with ESP32 core 2.0.11 to add `-zmuldefs` linker flag. This prevents multiple definition errors:
```bash
sed -i 's/compiler.c.elf.libs.esp32c3=/compiler.c.elf.libs.esp32c3=-zmuldefs /' platform.txt
sed -i 's/compiler.c.elf.libs.esp32s3=/compiler.c.elf.libs.esp32s3=-zmuldefs /' platform.txt
sed -i 's/compiler.c.elf.libs.esp32s2=/compiler.c.elf.libs.esp32s2=-zmuldefs /' platform.txt
sed -i 's/compiler.c.elf.libs.esp32=/compiler.c.elf.libs.esp32=-zmuldefs /' platform.txt
```

#### 2. zmuldefs for ESP32-C5 (Core 3.3.4)
For ESP32-C5 builds (core 3.3.4), use this instead:
```bash
sed -i 's/compiler.c.elf.extra_flags=/compiler.c.elf.extra_flags=-Wl,-zmuldefs /' platform.txt
```

#### 3. TFT_eSPI Configuration
**ALWAYS** copy appropriate `User_Setup_*.h` files to the TFT_eSPI library directory and modify `User_Setup_Select.h` to enable the correct configuration for each hardware variant. Example:
```bash
cp User_Setup_og_marauder.h CustomTFT_eSPI/
sed -i 's/^\/\/#include <User_Setup_og_marauder.h>/#include <User_Setup_og_marauder.h>/' CustomTFT_eSPI/User_Setup_Select.h
```

#### 4. TestFile Pre-Build Validation
The CI **ALWAYS** builds `TestFile/TestFile.ino` first as a validation step before building the main firmware. This is a simple test sketch that verifies the ESP32 core installation.

### Build Process Order (from CI)
1. Install Arduino CLI
2. Install ESP32 core for specific version (2.0.11 or 3.3.4)
3. Build TestFile.ino to validate ESP32 core installation
4. Install all required libraries from GitHub (clone to Custom* directories)
5. Configure TFT_eSPI (copy User_Setup files)
6. Install esptool via pip
7. Modify platform.txt with zmuldefs flags
8. Build main sketch with hardware-specific flags

### Build Command Example
```bash
arduino-cli compile \
  --fqbn esp32:esp32:d32:PartitionScheme=min_spiffs \
  --build-property compiler.cpp.extra_flags='-DMARAUDER_V4' \
  --warnings none \
  esp32_marauder/esp32_marauder.ino
```

### Hardware Variants and Build Flags
Each hardware variant requires a specific `-D` flag passed via `compiler.cpp.extra_flags`. Key variants:
- `MARAUDER_FLIPPER` - Flipper Zero WiFi Dev Board (ESP32-S2)
- `MARAUDER_V4`, `MARAUDER_V6`, `MARAUDER_V6_1`, `MARAUDER_V7` - Official Marauder boards
- `MARAUDER_M5STICKC`, `MARAUDER_M5STICKCP2` - M5Stack devices
- `MARAUDER_CARDPUTER` - M5 Cardputer
- `MARAUDER_CYD_MICRO`, `MARAUDER_CYD_2USB`, etc. - CYD (Cheap Yellow Display) variants
- `MARAUDER_C5` - ESP32-C5 DevKit (requires core 3.3.4)

### Flash Settings (from esp32_marauder.ino)
- **Board:** LOLIN D32 (or variant-specific)
- **Flash Frequency:** 80MHz
- **Partition Scheme:** Minimal SPIFFS
- **Flash Addresses:** 0x1000 (most boards), 0x2000 (ESP32-C5), 0x0 (some S3 boards)

## Build Instructions - Android App

### Prerequisites
- **Android Studio:** Latest version recommended
- **JDK:** 17 (required, specified in build.gradle.kts)
- **Android SDK:** API Level 34 (Android 14)
- **Gradle:** 8.1.4 (via wrapper)
- **Kotlin:** 1.9.20

### Build Commands
```bash
cd MarauderController

# Set ANDROID_HOME (required)
export ANDROID_HOME=$HOME/Android/Sdk  # Linux/Mac

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease

# Clean build
./gradlew clean
```

### Build Script
Use the provided `build.sh` script which validates ANDROID_HOME and provides helpful output:
```bash
./build.sh debug    # Build debug APK
./build.sh release  # Build release APK
./build.sh clean    # Clean build artifacts
```

### Output Locations
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Known Build Issues
- **ANDROID_HOME must be set** - The build will fail if this environment variable is not set
- **Gradle sync required** - First build requires internet to download dependencies
- **USB library dependency** - Uses `com.github.mik3y:usb-serial-for-android:v3.7.0` from JitPack

## GitHub Actions CI/CD

### Workflows
1. **`build_parallel.yml`** - Main build workflow
   - Triggers: push to master, tags, pull requests, manual dispatch
   - Builds all 19 hardware variants in parallel matrix
   - Takes ~15-20 minutes per variant
   - Uploads artifacts with naming: `esp32_marauder_<version>_<date>_<variant>.bin`
   - Creates draft releases on manual dispatch

2. **`nightly_build.yml`** - Nightly pre-release builds
   - Triggers: daily at 03:00 UTC, manual dispatch
   - Checks for new commits before building
   - Creates/updates "nightly" release tag
   - Adds "_beta_" to artifact names

3. **`close_stale.yml`** - Stale issue management

### CI Build Time Expectations
- **TestFile compilation:** ~30-60 seconds
- **Full firmware compilation per variant:** ~5-10 minutes
- **Complete matrix build (all variants):** ~15-20 minutes
- **Dependency installation:** ~2-3 minutes

### Artifact Naming Convention
```
esp32_marauder_v<version>_<date>_<hardware_variant>.bin
# Example: esp32_marauder_v1_9_0_20231227_flipper.bin
```

## Code Organization

### Main Firmware Components (`esp32_marauder/`)
- **`esp32_marauder.ino`** - Main entry point (setup/loop)
- **`configs.h`** - All build configurations, hardware targets, feature flags
- **`CommandLine.cpp/h`** - Serial command interface
- **`WiFiScan.cpp/h`** - WiFi scanning and attack functions
- **`EvilPortal.cpp/h`** - Captive portal functionality
- **`Display.cpp/h`** - TFT display handling (when HAS_SCREEN defined)
- **`MenuFunctions.cpp/h`** - Menu system
- **`SDInterface.cpp/h`** - SD card functionality
- **`BatteryInterface.cpp/h`** - Battery management
- **`GpsInterface.cpp/h`** - GPS module support

### Android App Components (`MarauderController/app/src/main/java/`)
- **`data/serial/`** - USB serial communication (SerialConnectionManager)
- **`data/protocol/`** - Marauder command protocol parser
- **`data/repository/`** - Data repository pattern
- **`ui/screens/`** - Compose UI screens (Connection, WiFi, Attacks, Lists, Settings)
- **`ui/viewmodel/`** - MVVM ViewModels

## Testing

### ESP32 Firmware
- **TestFile:** Simple validation sketch (`TestFile/TestFile.ino`) - used in CI to verify ESP32 core
- **No unit tests present** - Testing done via hardware validation

### Android App
- **JUnit 4.13.2** included for unit tests
- **Test directories:** `app/src/test/` and `app/src/androidTest/`
- No extensive test suite currently implemented

## Validation Checklist

When making changes to this repository:

1. **For ESP32 firmware changes:**
   - [ ] Identify which hardware variants are affected
   - [ ] Ensure board-specific feature flags in `configs.h` are respected
   - [ ] Verify TFT_eSPI configuration if display-related
   - [ ] Check if changes affect library dependencies
   - [ ] Consider flash size constraints (Minimal SPIFFS partition)
   - [ ] Test build with appropriate hardware flag (e.g., `-DMARAUDER_FLIPPER`)

2. **For Android app changes:**
   - [ ] Verify minimum SDK (API 24) compatibility
   - [ ] Ensure JDK 17 compatibility
   - [ ] Test with `./gradlew assembleDebug`
   - [ ] Verify USB serial communication is not broken
   - [ ] Check Material Design 3 consistency

3. **For build system changes:**
   - [ ] Test with both ESP32 core 2.0.11 and 3.3.4 if applicable
   - [ ] Verify zmuldefs workaround still applies
   - [ ] Check library version pinning
   - [ ] Validate workflow changes with manual dispatch

## Common Pitfalls and Solutions

### Issue: Multiple definition errors during ESP32 build
**Solution:** Ensure zmuldefs linker flag is added to platform.txt (see CRITICAL Build Workarounds)

### Issue: TFT_eSPI compilation errors
**Solution:** Verify correct User_Setup_*.h file is enabled in User_Setup_Select.h for target hardware

### Issue: Library not found errors
**Solution:** Check that all external libraries are installed with exact versions from workflow

### Issue: Android build fails with "ANDROID_HOME not set"
**Solution:** Export ANDROID_HOME environment variable pointing to Android SDK location

### Issue: ESP32-C5 build fails
**Solution:** Ensure using ESP32 core 3.3.4 and correct zmuldefs flag for extra_flags (not libs)

### Issue: Workflow runs out of time
**Solution:** The TestFile pre-build and individual variant builds can take 5-10 minutes each; ensure adequate timeout settings

## Important Notes

- **DO NOT modify vendored libraries** in `libraries/` or `esp32_marauder/libraries/` without updating workflow to match
- **ALWAYS pin library versions** when adding new dependencies - the CI relies on specific versions
- **Test hardware-specific changes** on actual hardware when possible - simulation is not available
- **Version number** is defined in `esp32_marauder/configs.h` as `MARAUDER_VERSION`
- **Releases are manual** via workflow_dispatch on build_parallel.yml - they create draft releases
- **Nightly builds are automated** but only run if new commits detected
- The repository includes **submodules** (.gitmodules present) - handle appropriately

## Quick Reference Commands

```bash
# ESP32 - Install Arduino CLI
curl -fsSL https://raw.githubusercontent.com/arduino/arduino-cli/master/install.sh | sh

# ESP32 - Install core
arduino-cli core install esp32:esp32@2.0.11 --additional-urls https://github.com/espressif/arduino-esp32/releases/download/2.0.11/package_esp32_dev_index.json

# Android - Build
cd MarauderController && ./gradlew assembleDebug

# Flash ESP32 (example for v4 hardware)
esptool.py --chip esp32 --port /dev/ttyUSB0 --baud 921600 write_flash -z --flash_mode dio --flash_freq 80m 0xe000 boot_app0.bin 0x1000 bootloader.bin 0x10000 firmware.bin 0x8000 partitions.bin

# Python flasher for ESP32-C5
cd C5_Py_Flasher && python c5_flasher.py
```

---

**Trust these instructions** - they are derived from the actual CI workflows and build configurations. Only search for additional information if you encounter behavior that contradicts these instructions or find them incomplete for your specific task.
