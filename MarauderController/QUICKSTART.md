# Quick Start Guide - ESP32 Marauder Controller

## What You Need

1. **Android phone/tablet** with USB OTG support (Android 7.0+)
2. **ESP32 Marauder device** (properly flashed with Marauder firmware)
3. **USB OTG cable/adapter** (USB-C to USB or Micro-USB to USB depending on your device)
4. **This app** (build it yourself or get a pre-built APK)

## Step 1: Build the App

### Option A: Using Android Studio (Recommended)

1. Install [Android Studio](https://developer.android.com/studio)
2. Clone this repository
3. Open the `MarauderController` folder in Android Studio
4. Click "Build" → "Build Bundle(s) / APK(s)" → "Build APK(s)"
5. Find the APK in `app/build/outputs/apk/debug/app-debug.apk`

### Option B: Using Command Line

```bash
cd MarauderController
export ANDROID_HOME=$HOME/Android/Sdk  # Adjust path for your system
./build.sh debug                        # Linux/Mac
# or
build.bat debug                         # Windows
```

The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

## Step 2: Install the App

### On Your Computer (via ADB)

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### On Your Android Device

1. Copy the APK to your Android device
2. Go to Settings → Security
3. Enable "Install from Unknown Sources" or "Install Unknown Apps" (varies by Android version)
4. Use a file manager to find and tap the APK file
5. Tap "Install"

## Step 3: Connect Your ESP32 Marauder

1. **Plug in your ESP32** to your Android device using a USB OTG cable
   - You should see a notification about a USB device connected
   
2. **Launch the Marauder Controller app**

3. **Grant USB Permission**
   - A dialog will appear asking for USB permission
   - Check "Always allow" if you want to skip this step in the future
   - Tap "OK"

4. **Connect to Device**
   - The app will show detected USB devices
   - Tap "Connect" next to your ESP32 device
   - The connection status at the top will change from red to green when connected

## Step 4: Using the App

### Scanning for WiFi Networks

1. Tap the **WiFi tab** at the bottom
2. Tap **"Scan APs"** to start scanning for access points
3. Watch the list populate with nearby WiFi networks
4. Tap **"Stop Scan"** when done
5. Tap **"Refresh List"** to reload the complete list from the device

### Running an Attack

⚠️ **WARNING**: Only use on networks you own or have permission to test!

1. Go to the **WiFi tab**
2. Scan for access points
3. **Select target(s)** by tapping on them (checkmark will appear)
4. Switch to the **Attacks tab**
5. Choose an attack type:
   - **Deauth Flood**: Disconnects clients from selected APs
   - **Probe Flood**: Floods APs with probe requests
   - **Beacon Spam**: Creates fake WiFi networks (requires SSID list)
   - **Rick Roll**: Special beacon spam with Rick Roll lyrics
6. Tap the attack button
7. Watch the terminal for live output
8. Tap **"STOP ATTACK"** to stop

### Managing SSIDs

1. Go to the **Lists tab**
2. Switch to **SSIDs** sub-tab
3. Type a custom SSID name and tap **"Add"**
4. Or tap **"Generate Random"** to create random SSIDs
5. Use these SSIDs for beacon spam attacks

### Viewing Device Info

1. Go to the **Settings tab**
2. Tap **"Refresh Info"** to get current device information
3. View firmware version, memory usage, and uptime

### Using the Terminal

1. Tap the **Terminal icon** (📟) in the top bar
2. View real-time output from the ESP32
3. Send custom commands directly to the device:
   - Type `help` to see all available commands
   - Type `scanap` to scan for access points
   - Type `list -a` to list captured access points
   - Type `stopscan` to stop current operation
4. Tap **"Clear"** to clear the terminal output

## Troubleshooting

### Device Not Detected

- **Check USB OTG support**: Not all Android devices support USB OTG. Test with another USB device first.
- **Try different cable**: Some cables are charge-only and don't support data transfer.
- **Check ESP32 power**: Make sure your ESP32 is powered on and the LED is lit.
- **Restart the app**: Close and reopen the app, then reconnect the USB cable.

### "No devices found"

- Tap **"Scan for Devices"** to refresh the device list
- Unplug and replug the USB cable
- Check if the ESP32 USB chip is supported (see README.md for supported chips)
- Try a different USB port on your OTG adapter

### Connection fails or disconnects

- Grant USB permission when prompted
- Try a different USB cable (data cable, not charge-only)
- Check if another app is using the USB connection
- Restart both the Android device and ESP32

### Commands not working

- Make sure you're connected (green indicator at top)
- Check the terminal for error messages
- Verify your ESP32 has the Marauder firmware installed
- Try sending a simple command like `help` in the terminal to test
- Restart the ESP32 by unplugging and replugging

### App crashes on start

- Clear app data: Settings → Apps → Marauder Controller → Storage → Clear Data
- Reinstall the app
- Check if you have Android 7.0 or higher

## Tips & Tricks

1. **Keep Terminal Open**: The terminal shows valuable real-time information about what's happening

2. **Select Multiple Targets**: You can select multiple access points before launching an attack

3. **Check Signal Strength**: Stronger signals (closer to 0, e.g., -30) are easier to attack than weaker ones (e.g., -90)

4. **Use Refresh**: After scanning, always tap "Refresh List" to see the complete results

5. **Monitor Battery**: WiFi attacks can drain battery quickly on both devices

6. **Save Your Work**: The ESP32 has commands to save captured data to SD card (if equipped)

## Legal Notice

⚠️ **IMPORTANT**: This tool is for **educational purposes** and **authorized security testing only**.

- Only use on networks you **own**
- Only use on networks where you have **written permission**
- Unauthorized access to computer networks is **illegal** in most jurisdictions
- You are **responsible** for how you use this tool

## Need Help?

- Check the [full README](README.md) for detailed information
- Visit the [ESP32 Marauder GitHub](https://github.com/justcallmekoko/ESP32Marauder) for firmware issues
- Open an issue on GitHub for app-specific problems

## Next Steps

- Explore the ESP32 Marauder's full command set (type `help` in terminal)
- Learn about different WiFi attack types and when to use them
- Read about WiFi security and how these attacks work
- Practice on your own test network to understand the tools

Happy (ethical) hacking! 🎭
