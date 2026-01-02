# BFFB
The BFFB is an expansion module for the Flipper Zero. It provides the Flipper Zero with access to external CC1101 capabilities, 2.4GHz tx/rx, and Marauder firmware.

## Marauder
With the onboard ESP32, GPS, and Micro SD card slot, users can run the Marauder firmware which allows them access to WiFi and Bluetooth analysis capabilities. For additional information regarding the features of the Marauder firmware, please read the rest of the project wiki. The BFFB uses the Dev Board Pro distribution of the Marauder firmware. This is relevant when it comes time to update the firmware.

## GPS
The GPS module is connected directly to the ESP32 module for the purpose of GPS data logging as well as wardriving. The GPS module is not connected to the Flipper Zero directly so Flipper apps like GPS will not work with this board.

## Dual CC1101
Included on the BFFB are two CC1101 modules. One is tuned for 400MHz band and the other for 900MHz band. They are both connected to the Flipper Zero SPI pins and not connected to the on-board ESP32. When using 3rd part firmware like unleashed or momentum for the Flipper Zero, you will have the option to use these external CC1101 modules for any subghz operations. This will allow the user access to a wider range of antenna and external amplifier options.

## NRF24
In addition to the other modules, the BFFB features an Ebyte NRF24 modules capable of 500mW transmission output. This module is connected to the Flipper Zero SPI pins and is utilized by 3rd party NRF24 apps like channel scan and mouser jacker.

## Module Switches
On the left side of the front of the BFFB are the module switches. These switches are used to switch between modules on the BFFB as more than one SPI module cannot be active at a time. The bottom switch is used to switch between NRF24 and ESP32. The top switch is to switch between 400MHz and 900MHz CC1101 modules. In order to access the CC1101 modules, the bottom switch must be in the ESP32 position. In order to use the ESP32 for Marauder, the switch must also be in the ESP32 position.

## Status LED
The BFFB features an on-board status LED which serves as an indicator for operational modes. Blue for any scanning or sniffing operations and red for any transmission operations. The LED can be disabled in the [settings](https://github.com/justcallmekoko/ESP32Marauder/wiki/marauder-settings).

## Flipper Zero
In order to use the BFFB, you will be required to use the [Marauder Companion App](https://github.com/0xchocolate/flipperzero-wifi-marauder/releases) on your Flipper Zero. This app provides you with an interface on your Flipper Zero to allow you to control the BFFB and its operations.

## Updating Firmware
Firmware for the ESP32 on the BFFB can be updated through USB via [FZEE Flasher](https://fzeeflasher.github.io/), Arduino IDE, through the GPIO pins via the ESP Flasher on the Flipper Zero using "Other WROOM", or via the [Micro SD card slot](https://github.com/justcallmekoko/ESP32Marauder/wiki/SD-Update). For more information regarding firmware updates, please see [Update Firmware](https://github.com/justcallmekoko/ESP32Marauder/wiki/update-firmware).

## Antenna Placement
The following documentation describes which antennas to connect to the different SMA connections on your Flipper Zero BFFB.  
_images coming soon_

From left to right: `2.4ghz, 400mhz, 900mhz, GPS, 2.4ghz`