# Spoof Airtag
Spoof Airtag is able to replicate data captured during an [Airtag Sniff](airtag-sniff) and broadcast it as Bluetooth Low Energy advertisements. The data advertised is an exact copy of the data captured during the sniff session. Upon a refresh of the Airtag data, the data stored in memory by the Marauder firmware will no longer match the official data of the legitimate Airtag. Use the following workflow documentation [Spoofing Airtags](spoofing-airtags).

## Menu Path
`Bluetooth`>`Bluetooth Attacks`>`Spoof Airtag`

## CLI
This function is available for use via the [Marauder CLI](https://github.com/justcallmekoko/ESP32Marauder/wiki/cli). The following documentation describes command usage. The Spoof Airtag function can be stopped with [stopscan](https://github.com/justcallmekoko/ESP32Marauder/wiki/stopscan). Before you can execute an Airtag Spoof, you must use the [list](list) command to get the index of the target Airtag.

### Usage
`spoofat -t <index>`