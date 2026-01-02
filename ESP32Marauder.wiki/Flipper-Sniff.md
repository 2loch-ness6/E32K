# Flipper Sniff
Flipper Sniff is able to detect Flipper Zero devices which are advertising their presence using Bluetooth Low Energy. Based on the UUID, we are able to determine the color of the Flipper Zero device. Other information like MAC address, rssi, and Flipper name is provided as well.

## Menu Path
`Bluetooth`>`Sniffers`>`Flipper Sniff`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage. The Flipper Sniff function can be stopped with [stopscan](https://github.com/justcallmekoko/ESP32Marauder/wiki/stopscan).

### Usage
`sniffbt -t flipper`