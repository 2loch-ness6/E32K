# Marauder Dev Board Pro
The Dev Board Pro is an expansion module for the Flipper Zero. It provides the Flipper Zero with access to Marauder firmware.

## Marauder
With the onboard ESP32 and Micro SD card slot, users can run the Marauder firmware which allows them access to WiFi and Bluetooth analysis capabilities. For additional information regarding the features of the Marauder firmware, please read the rest of the project wiki. The Dev Board Pro uses the Dev Board Pro distribution of the Marauder firmware. This is relevant when it comes time to update the firmware.

## GPS
While the Dev Board Pro does not natively include a GPS module, an expansion board can be purchased from [justcallmekokollc.com](https://justcallmekokollc.com) which adds this feature. Additional, we have documented the steps necessary to install a GPS Module yourself if you would like. The GPS module is responsible for providing features like location tracking and wardriving.

## Status LED
The Dev Board Pro features an on-board status LED which serves as an indicator for operational modes. Blue for any scanning or sniffing operations and red for any transmission operations. The LED can be disabled in the [settings](https://github.com/justcallmekoko/ESP32Marauder/wiki/marauder-settings).

## Flipper Zero
In order to use the Dev Board Pro, you will be required to use the [Marauder Companion App](https://github.com/0xchocolate/flipperzero-wifi-marauder/releases) on your Flipper Zero. This app provides you with an interface on your Flipper Zero to allow you to control the Dev Board Pro and its operations.

## Updating Firmware
Firmware for the ESP32 on the Dev Board Pro can be updated through through the GPIO pins via the ESP Flasher on the Flipper Zero using "Other WROOM", or via the [Micro SD card slot](https://github.com/justcallmekoko/ESP32Marauder/wiki/SD-Update). For more information regarding firmware updates, please see [Update Firmware](https://github.com/justcallmekoko/ESP32Marauder/wiki/update-firmware).