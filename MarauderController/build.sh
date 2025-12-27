#!/bin/bash

# Build script for ESP32 Marauder Controller Android App
# This script helps automate the build process

set -e  # Exit on error

echo "====================================="
echo "ESP32 Marauder Controller Build Script"
echo "====================================="
echo ""

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo "ERROR: ANDROID_HOME environment variable is not set."
    echo ""
    echo "Please set ANDROID_HOME to your Android SDK location:"
    echo "  export ANDROID_HOME=\$HOME/Android/Sdk  # Linux/Mac"
    echo "  or"
    echo "  set ANDROID_HOME=%LOCALAPPDATA%\\Android\\Sdk  # Windows"
    echo ""
    exit 1
fi

echo "✓ ANDROID_HOME: $ANDROID_HOME"
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "ERROR: gradlew not found. Are you in the MarauderController directory?"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Parse command line arguments
BUILD_TYPE="${1:-debug}"  # Default to debug build

case "$BUILD_TYPE" in
    debug)
        echo "Building DEBUG APK..."
        ./gradlew assembleDebug
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        ;;
    release)
        echo "Building RELEASE APK..."
        echo "Note: Release builds require signing configuration"
        ./gradlew assembleRelease
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        ;;
    clean)
        echo "Cleaning build artifacts..."
        ./gradlew clean
        echo "✓ Clean complete"
        exit 0
        ;;
    *)
        echo "Unknown build type: $BUILD_TYPE"
        echo "Usage: $0 [debug|release|clean]"
        exit 1
        ;;
esac

echo ""
echo "====================================="
echo "Build Complete!"
echo "====================================="
echo ""
echo "APK location: $APK_PATH"
echo ""

# Check if APK exists
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo "✓ APK Size: $APK_SIZE"
    echo ""
    echo "To install on a connected device:"
    echo "  adb install $APK_PATH"
    echo ""
    echo "Or copy the APK to your Android device and install manually."
else
    echo "✗ APK not found at expected location"
    echo "Build may have failed. Check the output above for errors."
    exit 1
fi
