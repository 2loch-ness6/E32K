# Packet Count
Packet Count shows the number of received WiFi frames from an access point or station. Only access points or stations that are marked as selected will be tracked which means you must [Scan APs](https://github.com/justcallmekoko/ESP32Marauder/wiki/scan-aps) and/or [Scan Stations](https://github.com/justcallmekoko/ESP32Marauder/wiki/scan-stations) and select them before using Packet Count. All other access points and stations will be ignored.

Packet Count does not generate PCAP data.

## Menu Path
`WiFi`>`Sniffers`>`Packet Count`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage. The Packet Count function can be stopped with [stopscan](https://github.com/justcallmekoko/ESP32Marauder/wiki/stopscan).

### Usage
`packetcount`