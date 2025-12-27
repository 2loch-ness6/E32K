#!/bin/bash

# Firmware Build Script for ESP32 Marauder
# Uses arduino-cli to compile the firmware source located in app/src/main/assets/firmware_source

FIRMWARE_SRC="app/src/main/assets/firmware_source"
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

# Install core if needed (this might require internet)
if ! arduino-cli core list | grep -q "esp32:esp32";
then
    echo "Installing ESP32 core..."
    arduino-cli core update-index --additional-urls https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
    arduino-cli core install esp32:esp32 --additional-urls https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
fi

# Build
echo "Compiling firmware..."
arduino-cli compile \
    --fqbn "$FQBN" \
    --libraries "$FIRMWARE_SRC/libraries" \
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
