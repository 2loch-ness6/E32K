# Shutdown WiFi
This menu option disconnects the user from any currently connected WLAN and de-initializes all WiFi interfaces.

## stopscan
Stops any currently running WiFi/Bluetooth scan/attack. Using `-f` (force) will disconnect your device from any currently connected WLANs. When using `stopscan` without `-f` during WLAN operations, it will only stop the current scan. It will not disconnect you from the WLAN.

## Usage
`stopscan [-f]`