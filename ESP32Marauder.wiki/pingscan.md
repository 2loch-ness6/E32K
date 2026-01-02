# Ping Scan
Perform a full ICMP scan of the connected network. The IP list is automatically generated based on the gateway and subnet mask of the connected network. All IPs of the subnet will be scanned by default. Active IPs will be displayed on the screen and in the serial monitor. They will also be stored in a list for future operations.

## Menu Path
`WiFi`>`Sniffers`>`Ping Scan`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage. The function can be stopped with [stopscan](https://github.com/justcallmekoko/ESP32Marauder/wiki/stopscan).

## Usage
```pingscan```