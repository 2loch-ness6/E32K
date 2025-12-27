#!/bin/bash

# Firmware Build Script for ESP32 Marauder
# Uses arduino-cli to compile the firmware source located in app/src/main/assets/esp32_marauder

FIRMWARE_SRC="app/src/main/assets/esp32_marauder"
BUILD_DIR="build_firmware_tmp"
OUTPUT_DIR="app/src/main/assets/firmware"
SKETCH_NAME="esp32_marauder.ino"
FQBN="esp32:esp32:esp32" # Generic ESP32, can be customized via args

# Check for arduino-cli
if ! command -v arduino-cli &> /dev/null;
then
    echo "Error: arduino-cli is not installed or not in PATH."
    echo "Please install it: https://arduino.github.io/arduino-cli/latest/installation/"
    exit 1
fi

echo "Found arduino-cli. Preparing build..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Configure configs.h for ESP32_LDDB (NodeMCU/Wemos)
echo "Configuring firmware for ESP32_LDDB..."
CONFIG_FILE="$FIRMWARE_SRC/configs.h"

# Uncomment ESP32_LDDB
sed -i 's/\/\/#define ESP32_LDDB/#define ESP32_LDDB/g' "$CONFIG_FILE"

# Comment out others (optional, but good practice if multiple were somehow enabled)
# Note: In C++, typically you'd just ensure one is active. The file has them commented out by default.

# Install core if needed (this might require internet)
if ! arduino-cli core list | grep -q "esp32:esp32";
then
    echo "Installing ESP32 core..."
    arduino-cli core update-index --additional-urls https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
    arduino-cli core install esp32:esp32 --additional-urls https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
fi

# Build
echo "Compiling firmware..."
arduino-cli compile --build-property "build.partitions=huge_app" --build-property "upload.maximum_size=3145728" \
    --fqbn "$FQBN" \
    --libraries "$FIRMWARE_SRC/libraries" --library "/root/Arduino/libraries" \
    --build-path "$BUILD_DIR" \
    --output-dir "$OUTPUT_DIR" \
    "$FIRMWARE_SRC/$SKETCH_NAME"

if [ $? -eq 0 ];
then
    echo "Build successful! Binaries are in $OUTPUT_DIR"
    ls -l "$OUTPUT_DIR"
else
    echo "Build failed."
    exit 1
fi
